from fastapi import APIRouter, Query, Depends
from sqlalchemy.orm import Session

from app.core.rawg import search_games, get_game_details, get_popular_games, get_genres
from app.schemas.game import GameDetailResponse

# ⬇️ importa la dependencia de DB y el upsert
from app.core.dependencies import get_db
from app.crud.game_catalog import upsert_game_catalog

router = APIRouter()

@router.get("/games/search")
async def search(query: str = Query(..., min_length=1)):
    return await search_games(query)

@router.get("/games/popular")
async def popular_games(page: int = 1):
    return await get_popular_games(page)

@router.get("/games/genres")
async def genres():
    return await get_genres()

@router.get("/games/{game_id}", response_model=GameDetailResponse)
async def get_game(
    game_id: int,
    db: Session = Depends(get_db),   # ⬅️ inyecta la sesión
):
    # 1) pides el detalle a RAWG como hacías
    detail = await get_game_details(game_id)

    # 2) extraes las features necesarias para el catálogo
    name = detail.name if hasattr(detail, "name") else getattr(detail, "name", "")
    # Si 'detail' es un dict en tu implementación, cambia por detail["name"] y usa .get(...)
    # Los siguientes 3 campos deben ser listas de strings
    try:
        genres = [g["name"] for g in detail.get("genres", [])]
        tags = [t["name"] for t in detail.get("tags", [])]
        platforms = [p["platform"]["name"] for p in detail.get("platforms", [])]
        metacritic = detail.get("metacritic")
        rating = detail.get("rating")
    except AttributeError:
        # Si GameDetailResponse es pydantic y trae objetos anidados
        genres = [g.name for g in (detail.genres or [])]
        tags = [t.name for t in (detail.tags or [])][:30]
        platforms = [p.platform.name for p in (detail.platforms or [])]
        metacritic = getattr(detail, "metacritic", None)
        rating = getattr(detail, "rating", None)

    # 3) guardas/actualizas el catálogo
    upsert_game_catalog(
        db=db,
        rawg_id=game_id,
        name=name or "",
        genres=genres,
        tags=tags[:30],           # limitar un poco ruido
        platforms=platforms,
        metacritic=metacritic,
        rating=rating,
    )

    # 4) devuelves el detalle como siempre
    return detail
