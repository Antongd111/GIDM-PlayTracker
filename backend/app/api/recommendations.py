from fastapi import APIRouter, Depends, Query
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

# ----- Pesos por estado del juego -----
# Solo 3 estados: jugado/terminado con peso alto, por jugar con peso bajo.
STATUS_WEIGHT: Dict[str, float] = {
    "jugando": 1.0,
    "completado": 1.0,
    "por jugar": 0.3,
}
DEFAULT_STATUS_WEIGHT = 0.4  # Valor por defecto si llega algo inesperado

# Parámetros por defecto
K_REPRESENTATIVE_DEFAULT = 20  # nº de juegos representativos del usuario
G_TOP_GENRES_DEFAULT = 2       # nº de géneros más fuertes del perfil
PAGES_PER_GENRE_DEFAULT = 2    # nº de páginas de RAWG a consultar por género


# ------------------- Funciones auxiliares -------------------

def _normalize_score(score: int | None) -> float:
    """
    Normaliza un score [0,100] a [0,1].
    Si es None, devuelve 0.
    """
    if score is None:
        return 0.0
    score = max(0, min(score, 100))
    return score / 100.0


def _calculate_game_weight(user_game: UserGame) -> float:
    """
    Devuelve el peso de un juego en función de su estado y puntuación.
    """
    status_key = (user_game.status or "").strip().lower()
    base_weight = STATUS_WEIGHT.get(status_key, DEFAULT_STATUS_WEIGHT)
    # Mezcla estado (peso base) con score del usuario si existe.
    return base_weight * (0.5 + 0.5 * _normalize_score(user_game.score))


def _parse_year(value) -> int:
    """
    Intenta extraer el año (int) de una fecha en distintos formatos.
    """
    if value is None:
        return 0
    if isinstance(value, int):
        return value
    value_str = str(value)
    return int(value_str[:4]) if len(value_str) >= 4 and value_str[:4].isdigit() else 0


def _preview_from_detail(game_id: int, detail: Dict[str, Any]) -> GamePreview:
    """
    Convierte el detalle de un juego de RAWG en un objeto GamePreview simplificado.
    """
    title = detail.get("title") or detail.get("name") or detail.get("slug") or f"RAWG-{game_id}"
    image_url = detail.get("imageUrl") or detail.get("background_image") or ""
    released = detail.get("releaseDate") or detail.get("released")
    year = _parse_year(released)
    return GamePreview(id=game_id, title=str(title), imageUrl=str(image_url), year=year)


async def _fetch_previews(rawg_ids: List[int]) -> List[GamePreview]:
    """
    Dada una lista de IDs de juegos en RAWG, devuelve una lista de GamePreview.
    Usa semáforo para limitar concurrencia.
    """
    semaphore = asyncio.Semaphore(8)

    async def fetch_one(game_id: int) -> GamePreview:
        async with semaphore:
            try:
                detail = await get_game_details(game_id)
                if not isinstance(detail, dict):
                    detail = detail.__dict__ if hasattr(detail, "__dict__") else {}
                return _preview_from_detail(game_id, detail)
            except Exception as e:
                print(f"[fetch_previews] ERROR get_game_details({game_id}): {e}")
                return GamePreview(id=game_id, title=f"RAWG-{game_id}", imageUrl="", year=0)

    print(f"[fetch_previews] fetching {len(rawg_ids)} ids")
    return await asyncio.gather(*(fetch_one(g) for g in rawg_ids))


def _build_user_profile(
    owned_details: List[Dict[str, Any]],
    representative_games: List[UserGame]
) -> Tuple[Dict[str, float], Dict[str, float]]:
    """
    Construye el perfil del usuario en base a géneros y etiquetas.
    Devuelve:
        - afinidad por géneros (normalizada),
        - afinidad por tags (normalizada).
    """
    status_map = {ug.game_rawg_id: (ug.status or "").strip().lower() for ug in representative_games}
    score_map = {ug.game_rawg_id: ug.score for ug in representative_games}

    genre_affinity: Dict[str, float] = defaultdict(float)
    tag_affinity: Dict[str, float] = defaultdict(float)

    for game in owned_details:
        rawg_id = game.get("id")
        if rawg_id is None:
            continue

        status = status_map.get(rawg_id, "")
        weight = STATUS_WEIGHT.get(status, DEFAULT_STATUS_WEIGHT) * (0.5 + 0.5 * _normalize_score(score_map.get(rawg_id)))

        # Géneros
        for genre in game.get("genres", []) or []:
            genre_name = genre["name"] if isinstance(genre, dict) and "name" in genre else str(genre)
            genre_affinity[genre_name] += weight

        # Tags (solo top 10)
        for tag in (game.get("tags", []) or [])[:10]:
            tag_name = tag["name"] if isinstance(tag, dict) and "name" in tag else str(tag)
            tag_affinity[tag_name] += 0.5 * weight

    def _normalize_dict(d: Dict[str, float]) -> Dict[str, float]:
        norm = sqrt(sum(v*v for v in d.values())) or 1.0
        for key in list(d.keys()):
            d[key] = d[key] / norm
        return d

    print(f"[profile] genres={len(genre_affinity)} tags={len(tag_affinity)}")
    return _normalize_dict(genre_affinity), _normalize_dict(tag_affinity)


def _score_candidate_relevance(
    raw_candidate: Dict[str, Any],
    genre_affinity: Dict[str, float],
    tag_affinity: Dict[str, float],
) -> float:
    """
    Calcula la relevancia del candidato combinando:
    - coincidencia por géneros (60%)
    - coincidencia por tags (20%)
    - metacritic (15%)
    - frescura inversa por año (5%)
    """
    # Extraer características del candidato con tolerancia a formatos
    genres = [x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (raw_candidate.get("genres") or [])]
    tags = [x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (raw_candidate.get("tags") or [])[:10]]

    metacritic_norm = (raw_candidate.get("metacritic") or 0) / 100.0

    # 'Recency' inversa: a más antiguo, menos punto, con tope a 20 años
    year = _parse_year(raw_candidate.get("released")) if raw_candidate.get("released") else 0
    recency_penalty = 0.0 if not year else min(1.0, max(0.0, (2025 - year) / 20.0))  # 0..1

    # Similitud coseno simplificada con normalización por raíz
    genre_match = sum(genre_affinity.get(g, 0.0) for g in genres) / (sqrt(len(genres)) or 1.0)
    tag_match = sum(tag_affinity.get(t, 0.0) for t in tags) / (sqrt(len(tags)) or 1.0)

    score = 0.6 * genre_match + 0.2 * tag_match + 0.15 * metacritic_norm + 0.05 * (1 - recency_penalty)
    return score


def _maximal_marginal_relevance(
    candidates: List[Dict[str, Any]],
    candidate_scores: Dict[int, float],
    k: int = 10,
    lambda_relevance: float = 0.75
) -> List[Dict[str, Any]]:
    """
    Selecciona k elementos maximizando la relevancia (según 'candidate_scores') y
    minimizando redundancia entre seleccionados (diversidad) usando MMR.
    """
    def _similarity(a: Dict[str, Any], b: Dict[str, Any]) -> float:
        # Similaridad basada en intersección de géneros y (0.5*) de tags
        a_genres = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (a.get("genres") or []))
        a_tags = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (a.get("tags") or [])[:10])
        b_genres = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (b.get("genres") or []))
        b_tags = set(x["name"] if isinstance(x, dict) and "name" in x else str(x) for x in (b.get("tags") or [])[:10])

        inter = len(a_genres & b_genres) + 0.5 * len(a_tags & b_tags)
        denom_a = sqrt(len(a_genres) + 0.5 * len(a_tags)) or 1.0
        denom_b = sqrt(len(b_genres) + 0.5 * len(b_tags)) or 1.0
        return inter / (denom_a * denom_b)

    selected: List[Dict[str, Any]] = []
    pool = candidates[:]

    while pool and len(selected) < k:
        best_item, best_value = None, float("-inf")

        for candidate in pool:
            rid = candidate.get("id")
            relevance = candidate_scores.get(int(rid), 0.0)
            # Diversidad = máxima similitud con lo ya elegido
            diversity = max([_similarity(candidate, x) for x in selected], default=0.0)
            # MMR
            value = lambda_relevance * relevance - (1 - lambda_relevance) * diversity

            if value > best_value:
                best_item, best_value = candidate, value

        if best_item is None:
            break

        selected.append(best_item)
        pool = [x for x in pool if x.get("id") != best_item.get("id")]

    return selected


# ------------------- Endpoint principal -------------------

@router.get("/{user_id}", response_model=List[GamePreview])
async def recommend_for_user(
    user_id: int,
    top_k: int = Query(10, ge=1, le=50),
    k_representative: int = Query(K_REPRESENTATIVE_DEFAULT, ge=5, le=50),
    g_top_genres: int = Query(G_TOP_GENRES_DEFAULT, ge=1, le=5),
    pages_per_genre: int = Query(PAGES_PER_GENRE_DEFAULT, ge=1, le=3),
    db: AsyncSession = Depends(get_db),
):
    """
    Endpoint que devuelve recomendaciones de juegos para un usuario concreto.
    - Si no tiene juegos, se hace *cold-start* con el catálogo.
    - Si tiene juegos, se construye su perfil y se buscan candidatos por géneros,
      se puntúan y se diversifican (MMR).
    """
    print(f"[request] user_id={user_id}, top_k={top_k}, K={k_representative}, G={g_top_genres}, P={pages_per_genre}")

    # --- Paso 0: Obtener juegos del usuario ---
    res = await db.execute(select(UserGame).where(UserGame.user_id == user_id))
    user_games: List[UserGame] = list(res.scalars().all())
    print(f"[step0] user_games={len(user_games)}")
    if user_games:
        print("[step0] sample:", [f"{ug.game_rawg_id}:{(ug.status or '').strip().lower()}:{ug.score}" for ug in user_games[:5]])

    if not user_games:
        # Cold-start: recomendar mejores juegos del catálogo
        print("[cold-start] Usuario sin juegos -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k)
        )
        ids = [int(x[0]) for x in res2.all()]
        print(f"[cold-start] ids={ids}")
        return await _fetch_previews(ids)

    # --- Paso 1: Selección de juegos representativos ---
    sorted_by_weight = sorted(user_games, key=_calculate_game_weight, reverse=True)
    representative_games = sorted_by_weight[:k_representative]
    owned_ids = {int(x.game_rawg_id) for x in user_games}
    print(f"[step1] representative={len(representative_games)}, owned={len(owned_ids)}")

    # --- Paso 2: Detalles de juegos representativos desde RAWG ---
    semaphore = asyncio.Semaphore(8)

    async def _get_detail(gid: int) -> Dict[str, Any]:
        async with semaphore:
            try:
                detail = await get_game_details(gid)
                return detail if isinstance(detail, dict) else (detail.__dict__ if hasattr(detail, "__dict__") else {})
            except Exception as e:
                print(f"[step2] ERROR get_game_details({gid}): {e}")
                return {"id": gid, "genres": [], "tags": [], "developers": []}

    print(f"[step2] fetching details for {len(representative_games)}")
    owned_details: List[Dict[str, Any]] = await asyncio.gather(
        *(_get_detail(int(ug.game_rawg_id)) for ug in representative_games)
    )
    ok_details = sum(1 for d in owned_details if d.get("id") is not None)
    print(f"[step2] owned_details OK={ok_details}/{len(owned_details)}")

    # --- Paso 3: Construir perfil de usuario ---
    genre_affinity, tag_affinity = _build_user_profile(owned_details, representative_games)
    print(f"[step3] top genres: {sorted(genre_affinity.items(), key=lambda x: x[1], reverse=True)[:5]}")
    print(f"[step3] top tags: {sorted(tag_affinity.items(), key=lambda x: x[1], reverse=True)[:5]}")

    # Si el perfil queda vacío, fallback a catálogo (excluyendo propios)
    if not genre_affinity and not tag_affinity:
        print("[step3] perfil vacío -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k * 2)
        )
        ids = [int(x[0]) for x in res2.all() if int(x[0]) not in owned_ids][:top_k]
        print(f"[fallback-catalog] ids={ids}")
        return await _fetch_previews(ids)

    # --- Paso 4: Obtener candidatos por géneros dominantes ---
    top_genres = [k for k, _ in sorted(genre_affinity.items(), key=lambda x: x[1], reverse=True)[:max(g_top_genres, 1)]]
    print(f"[step4] querying RAWG by genres={top_genres}")

    raw_candidates: List[Dict[str, Any]] = []
    for page in range(1, pages_per_genre + 1):
        try:
            page_results = await list_games_by_genres(
                top_genres, page=page, page_size=40, ordering="-metacritic"
            )
            print(f"[step4] page={page} raw_candidates={len(page_results)}")
        except Exception as e:
            print(f"[step4] ERROR list_games_by_genres(page={page}): {e}")
            page_results = []

        added_this_page = 0
        for cand in page_results:
            rid = cand.get("id")
            if rid and int(rid) not in owned_ids:
                raw_candidates.append(cand)
                added_this_page += 1
        print(f"[step4] page={page} added={added_this_page}, total={len(raw_candidates)}")

    if not raw_candidates:
        print("[step4] sin candidatos -> catálogo")
        res2 = await db.execute(
            select(GameCatalog.game_rawg_id).order_by(GameCatalog.rating.desc().nullslast()).limit(top_k * 2)
        )
        ids = [int(x[0]) for x in res2.all() if int(x[0]) not in owned_ids][:top_k]
        print(f"[fallback-catalog-2] ids={ids}")
        return await _fetch_previews(ids)

    # --- Paso 5: Scoring y diversificación (MMR) ---
    candidate_scores: Dict[int, float] = {
        int(c.get("id")): _score_candidate_relevance(c, genre_affinity, tag_affinity)
        for c in raw_candidates if c.get("id")
    }

    # Orden preliminar solo para log/diagnóstico
    raw_candidates.sort(key=lambda c: candidate_scores.get(int(c.get("id", 0)), 0.0), reverse=True)
    print(
        "[step5] top5 scores:",
        [f"{c.get('id')}:{candidate_scores.get(int(c.get('id', 0)), 0):.3f}" for c in raw_candidates[:5]]
    )

    diversified_selection = _maximal_marginal_relevance(
        raw_candidates, candidate_scores, k=top_k, lambda_relevance=0.75
    )
    recommended_ids = [int(c["id"]) for c in diversified_selection if c.get("id")]
    print(f"[result] rec_ids={recommended_ids}")

    # --- Paso 6: Mapear a previews (mínimas llamadas a RAWG) ---
    return await _fetch_previews(recommended_ids)
