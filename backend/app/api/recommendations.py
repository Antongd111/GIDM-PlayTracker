from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Tuple, Dict, Any
import asyncio
from math import sqrt
from collections import defaultdict

from app.core.dependencies import get_db
from app.models.user_game import UserGame
from app.models.game_catalog import GameCatalog
from app.schemas.game import GamePreview
from app.core.rawg import get_game_details, list_games_by_genres

router = APIRouter(prefix="/recommendations", tags=["recommendations"])

# ----- Pesos normalizados (minúsculas) -----
STATUS_WEIGHT: Dict[str, float] = {
    "jugando": 1.0, "playing": 1.0,
    "completado": 1.0, "terminado": 1.0, "completed": 1.0,
    "pausado": 0.5, "paused": 0.5,
    "abandonado": 0.2, "dropped": 0.2,
    "por jugar": 0.3, "pendiente": 0.3, "backlog": 0.3, "plan_to_play": 0.3,
}
DEFAULT_STATUS_WEIGHT = 0.4

K_REPRESENTATIVE_DEFAULT = 20
G_TOP_GENRES_DEFAULT = 2
PAGES_PER_GENRE_DEFAULT = 2

def _score01(score: int | None) -> float:
    if score is None: return 0.0
    s = 0 if score < 0 else (100 if score > 100 else score)
    return s / 100.0

def _weight_for_user_game(ug: UserGame) -> float:
    key = (ug.status or "").strip().lower()
    w_status = STATUS_WEIGHT.get(key, DEFAULT_STATUS_WEIGHT)
    return w_status * (0.5 + 0.5 * _score01(ug.score))

def _parse_year(val) -> int:
    if val is None: return 0
    if isinstance(val, int): return val
    s = str(val)
    return int(s[:4]) if len(s) >= 4 and s[:4].isdigit() else 0

def _preview_from_detail(gid: int, detail: Dict[str, Any]) -> GamePreview:
    title = detail.get("title") or detail.get("name") or detail.get("slug") or f"RAWG-{gid}"
    image = detail.get("imageUrl") or detail.get("background_image") or ""
    released = detail.get("releaseDate") or detail.get("released")
    year = _parse_year(released)
    return GamePreview(id=gid, title=str(title), imageUrl=str(image), year=year)

async def _fetch_previews(ids: List[int]) -> List[GamePreview]:
    sem = asyncio.Semaphore(8)
    async def fetch_one(gid: int) -> GamePreview:
        async with sem:
            try:
                detail = await get_game_details(gid)
                if not isinstance(detail, dict):
                    detail = detail.__dict__ if hasattr(detail, "__dict__") else {}
                return _preview_from_detail(gid, detail)
            except Exception as e:
                print(f"[fetch_previews] get_game_details({gid}) ERROR: {e}")
                return GamePreview(id=gid, title=f"RAWG-{gid}", imageUrl="", year=0)
    print(f"[fetch_previews] fetching {len(ids)} ids")
    return await asyncio.gather(*(fetch_one(g) for g in ids))

def _build_user_profile_from_owned_enriched(
    owned_enriched: List[Dict[str, Any]],
    top_user_games: List[UserGame]
) -> Tuple[Dict[str, float], Dict[str, float], set]:
    st_map = {ug.game_rawg_id: (ug.status or "").strip().lower() for ug in top_user_games}
    sc_map = {ug.game_rawg_id: ug.score for ug in top_user_games}
    genre_aff: Dict[str, float] = defaultdict(float)
    tag_aff: Dict[str, float] = defaultdict(float)
    dev_drop: set = set()

    for g in owned_enriched:
        rid = g.get("id")
        if rid is None: continue
        st = st_map.get(rid, "")
        w = STATUS_WEIGHT.get(st, DEFAULT_STATUS_WEIGHT) * (0.5 + 0.5 * _score01(sc_map.get(rid)))
        for x in g.get("genres", []) or []:
            name = x["name"] if isinstance(x, dict) and "name" in x else str(x)
            genre_aff[name] += w
        for x in (g.get("tags", []) or [])[:10]:
            name = x["name"] if isinstance(x, dict) and "name" in x else str(x)
            tag_aff[name] += 0.5 * w
        if st in ("abandonado", "dropped"):
            for d in g.get("developers", []) or []:
                name = d["name"] if isinstance(d, dict) and "name" in d else str(d)
                dev_drop.add(name)

    def _norm(d: Dict[str, float]) -> Dict[str, float]:
        from math import sqrt
        norm = sqrt(sum(v*v for v in d.values())) or 1.0
        for k in list(d.keys()):
            d[k] = d[k] / norm
        return d

    print(f"[profile] genres={len(genre_aff)} tags={len(tag_aff)} dev_drop={len(dev_drop)}")
    return _norm(genre_aff), _norm(tag_aff), dev_drop

def _score_candidate(raw: Dict[str, Any], g_aff: Dict[str, float], t_aff: Dict[str, float], dev_drop: set) -> float:
    from math import sqrt
    genres = [x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (raw.get("genres") or [])]
    tags = [x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (raw.get("tags") or [])[:10]]
    devs = [x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (raw.get("developers") or [])]
    metas = (raw.get("metacritic") or 0) / 100.0
    year = _parse_year(raw.get("released")) if raw.get("released") else 0
    rec = 0.0 if not year else min(1.0, max(0.0, (2025 - year) / 20.0))
    genre_match = sum(g_aff.get(g, 0.0) for g in genres) / (sqrt(len(genres)) or 1.0)
    tag_match = sum(t_aff.get(t, 0.0) for t in tags) / (sqrt(len(tags)) or 1.0)
    s = 0.6*genre_match + 0.2*tag_match + 0.15*metas + 0.05*(1 - rec)
    if any(d in dev_drop for d in devs): s *= 0.85
    return s

def _mmr(cands: List[Dict[str, Any]], scores: Dict[int, float], k: int = 10, lam: float = 0.75) -> List[Dict[str, Any]]:
    from math import sqrt
    chosen: List[Dict[str, Any]] = []
    def sim(a: Dict[str, Any], b: Dict[str, Any]) -> float:
        ag = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (a.get("genres") or []))
        at = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (a.get("tags") or [])[:10])
        bg = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (b.get("genres") or []))
        bt = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (b.get("tags") or [])[:10])
        inter = len(ag & bg) + 0.5 * len(at & bt)
        den = (sqrt(len(ag) + 0.5 * len(at)) * sqrt(len(bg) + 0.5 * len(bt))) or 1.0
        return inter / den
    pool = cands[:]
    while pool and len(chosen) < k:
        best, best_val = None, -1e9
        for c in pool:
            rid = c.get("id")
            rel = scores.get(rid, 0.0)
            div = max([sim(c, x) for x in chosen], default=0.0)
            val = lam*rel - (1-lam)*div
            if val > best_val: best, best_val = c, val
        if best is None: break
        chosen.append(best)
        pool = [x for x in pool if x.get("id") != best.get("id")]
    return chosen

@router.get("/{user_id}", response_model=List[GamePreview])
async def recommend_for_user(
    user_id: int,
    top_k: int = Query(10, ge=1, le=50),
    k_representative: int = Query(K_REPRESENTATIVE_DEFAULT, ge=5, le=50),
    g_top_genres: int = Query(G_TOP_GENRES_DEFAULT, ge=1, le=5),
    pages_per_genre: int = Query(PAGES_PER_GENRE_DEFAULT, ge=1, le=3),
    db: AsyncSession = Depends(get_db),
):
    print(f"[request] user_id={user_id}, top_k={top_k}, K={k_representative}, G={g_top_genres}, P={pages_per_genre}")

    # 0) user games
    res = await db.execute(select(UserGame).where(UserGame.user_id == user_id))
    user_games: List[UserGame] = list(res.scalars().all())
    print(f"[step0] user_games={len(user_games)}")
    if user_games:
        print("[step0] sample:", [f"{ug.game_rawg_id}:{(ug.status or '').strip().lower()}:{ug.score}" for ug in user_games[:5]])

    if not user_games:
        print("[cold-start] sin juegos -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k)
        )
        ids = [int(x[0]) for x in res2.all()]
        print(f"[cold-start] ids={ids}")
        return await _fetch_previews(ids)

    # 1) K representativos
    sorted_by_rep = sorted(user_games, key=_weight_for_user_game, reverse=True)
    top_ugs = sorted_by_rep[:k_representative]
    owned_ids = {int(x.game_rawg_id) for x in user_games}
    print(f"[step1] top_ugs={len(top_ugs)}, owned_ids={len(owned_ids)}")

    # 2) Detalles RAWG (con try/except por id)
    sem = asyncio.Semaphore(8)
    async def _get_detail(gid: int) -> Dict[str, Any]:
        async with sem:
            try:
                d = await get_game_details(gid)
                return d if isinstance(d, dict) else (d.__dict__ if hasattr(d, "__dict__") else {})
            except Exception as e:
                print(f"[step2] ERROR get_game_details({gid}): {e}")
                return {"id": gid, "genres": [], "tags": [], "developers": []}
    print(f"[step2] fetching details for {len(top_ugs)}")
    owned_details: List[Dict[str, Any]] = await asyncio.gather(*(_get_detail(int(ug.game_rawg_id)) for ug in top_ugs))
    ok_details = sum(1 for d in owned_details if d.get("id") is not None)
    print(f"[step2] owned_details OK={ok_details}/{len(owned_details)}")

    # 3) Perfil
    g_aff, t_aff, dev_drop = _build_user_profile_from_owned_enriched(owned_details, top_ugs)
    print(f"[step3] top genres: {sorted(g_aff.items(), key=lambda x: x[1], reverse=True)[:5]}")
    print(f"[step3] top tags: {sorted(t_aff.items(), key=lambda x: x[1], reverse=True)[:5]}")

    if not g_aff and not t_aff:
        print("[step3] perfil vacío -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k)
        )
        ids = [int(x[0]) for x in res2.all() if int(x[0]) not in owned_ids][:top_k]
        print(f"[fallback-catalog] ids={ids}")
        return await _fetch_previews(ids)

    # 4) Candidatos por géneros
    top_genres = [k for k, _ in sorted(g_aff.items(), key=lambda x: x[1], reverse=True)[:max(g_top_genres, 1)]]
    print(f"[step4] querying RAWG genres={top_genres}")
    candidates: List[Dict[str, Any]] = []
    for p in range(1, pages_per_genre + 1):
        try:
            result_page = await list_games_by_genres(top_genres, page=p, page_size=40, ordering="-metacritic")
            print(f"[step4] page={p} raw_candidates={len(result_page)}")
        except Exception as e:
            print(f"[step4] ERROR list_games_by_genres(page={p}): {e}")
            result_page = []
        added = 0
        for c in result_page:
            rid = c.get("id")
            if rid and int(rid) not in owned_ids:
                candidates.append(c)
                added += 1
        print(f"[step4] page={p} added={added}, total={len(candidates)}")

    if not candidates:
        print("[step4] sin candidatos -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k)
        )
        ids = [int(x[0]) for x in res2.all() if int(x[0]) not in owned_ids][:top_k]
        print(f"[fallback-catalog-2] ids={ids}")
        return await _fetch_previews(ids)

    # 5) Score + MMR
    scores = {int(c.get("id")): _score_candidate(c, g_aff, t_aff, dev_drop) for c in candidates if c.get("id")}
    candidates.sort(key=lambda c: scores.get(int(c.get("id", 0)), 0.0), reverse=True)
    print("[step5] top5 scores:", [f"{c.get('id')}:{scores.get(int(c.get('id',0)),0):.3f}" for c in candidates[:5]])

    selected = _mmr(candidates, scores, k=top_k, lam=0.75)
    rec_ids = [int(c["id"]) for c in selected if c.get("id")]
    print(f"[result] rec_ids={rec_ids}")

    return await _fetch_previews(rec_ids)
