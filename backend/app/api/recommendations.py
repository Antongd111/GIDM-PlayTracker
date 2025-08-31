from fastapi import APIRouter, Depends, Query
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from scipy import sparse
import numpy as np
import pandas as pd
import joblib
from typing import List
import asyncio
import re

from app.core.dependencies import get_db
from app.models.user_game import UserGame
from app.models.game_catalog import GameCatalog
from app.schemas.game import GamePreview
from app.core.rawg import get_game_details

router = APIRouter(prefix="/recommendations", tags=["recommendations"])

vectorizer = None
ITEM_MATRIX = None
ITEM_IDS = None


def _load_models():
    global vectorizer, ITEM_MATRIX, ITEM_IDS
    if vectorizer is None:
        vectorizer = joblib.load("model_store/tfidf_vectorizer.joblib")
    if ITEM_MATRIX is None:
        ITEM_MATRIX = sparse.load_npz("model_store/item_matrix.npz").tocsr()
    if ITEM_IDS is None:
        ITEM_IDS = pd.read_csv("model_store/item_ids.csv")["game_rawg_id"].to_numpy()


def _cosine(a_row: sparse.csr_matrix, B: sparse.csr_matrix) -> np.ndarray:
    numer = a_row.dot(B.T).toarray().ravel()
    a_norm = np.sqrt(a_row.multiply(a_row).sum()) + 1e-9
    b_norm = np.sqrt(B.multiply(B).sum(axis=1)).A1 + 1e-9
    return numer / (a_norm * b_norm)


_year_re = re.compile(r"^\s*(\d{4})")

def _parse_year(val) -> int:
    """
    Acepta: 'YYYY-MM-DD', 'YYYY', None. Devuelve año entero o 0 si no válido.
    """
    if val is None:
        return 0
    if isinstance(val, int):
        return val
    s = str(val)
    m = _year_re.match(s)
    if not m:
        return 0
    try:
        return int(m.group(1))
    except Exception:
        return 0


def _preview_from_detail(gid: int, detail) -> GamePreview:
    """
    Normaliza un detalle (dict o modelo) a GamePreview con year:int.
    """
    if isinstance(detail, dict):
        title = detail.get("title") or detail.get("name") or detail.get("slug") or f"RAWG-{gid}"
        image = detail.get("imageUrl") or detail.get("background_image") or ""
        # RAWG suele usar 'released'
        released = detail.get("releaseDate") or detail.get("released")
    else:
        title = (
            getattr(detail, "title", None)
            or getattr(detail, "name", None)
            or getattr(detail, "slug", None)
            or f"RAWG-{gid}"
        )
        image = getattr(detail, "imageUrl", None) or getattr(detail, "background_image", None) or ""
        released = getattr(detail, "releaseDate", None) or getattr(detail, "released", None)

    year = _parse_year(released)
    return GamePreview(id=gid, title=str(title), imageUrl=str(image), year=year)


async def _fetch_previews(ids: List[int]) -> List[GamePreview]:
    sem = asyncio.Semaphore(8)

    async def fetch_one(gid: int) -> GamePreview:
        async with sem:
            try:
                detail = await get_game_details(gid)
                return _preview_from_detail(gid, detail)
            except Exception:
                # fallback robusto
                return GamePreview(id=gid, title=f"RAWG-{gid}", imageUrl="", year=0)

    return await asyncio.gather(*(fetch_one(g) for g in ids))


@router.get("/{user_id}", response_model=List[GamePreview])
async def recommend_for_user(
    user_id: int,
    top_k: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    _load_models()

    # Juegos del usuario con score válido y en playing/completed
    res = await db.execute(
        select(UserGame).where(
            (UserGame.user_id == user_id)
            & (UserGame.score.isnot(None))
            & (UserGame.score > 0)
            & (UserGame.status.in_(["playing", "completed"]))
        )
    )
    ugs = res.scalars().all()

    # Cold-start: top por rating del catálogo
    if not ugs:
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id)
            .order_by(GameCatalog.rating.desc().nullslast())
            .limit(top_k)
        )
        ids = [int(x[0]) for x in res2.all()]
        return await _fetch_previews(ids)

    # Perfil del usuario
    id_to_idx = {int(gid): i for i, gid in enumerate(ITEM_IDS)}
    pairs = [
        (id_to_idx[int(ug.game_rawg_id)], float(ug.score) / 100.0)
        for ug in ugs
        if int(ug.game_rawg_id) in id_to_idx
    ]
    if not pairs:
        return []

    idxs, w = zip(*pairs)
    w = np.array(w, dtype=np.float32)
    user_profile = (ITEM_MATRIX[list(idxs), :].multiply(w[:, None])).sum(axis=0)
    user_profile = sparse.csr_matrix(user_profile)

    # Similitud + filtrado de ya vistos
    sims = _cosine(user_profile, ITEM_MATRIX)
    rated = {int(ug.game_rawg_id) for ug in ugs}
    mask = np.array([gid not in rated for gid in ITEM_IDS])
    sims_f, ids_f = sims[mask], ITEM_IDS[mask]
    if sims_f.size == 0:
        return []

    k = min(top_k, sims_f.size)
    part = np.argpartition(-sims_f, k - 1)[:k]
    order = np.argsort(-sims_f[part])
    sel = part[order]
    rec_ids = [int(ids_f[i]) for i in sel]

    # Devuelve GamePreview con year:int
    return await _fetch_previews(rec_ids)
