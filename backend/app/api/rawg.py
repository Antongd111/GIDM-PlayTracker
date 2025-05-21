from fastapi import APIRouter, Query
from app.core.rawg import search_games, get_game_details, get_popular_games, get_genres

router = APIRouter()

@router.get("/games/search")
async def search(query: str = Query(..., min_length=1)):
    return await search_games(query)

@router.get("/games/{game_id}")
async def get_game(game_id: int):
    return await get_game_details(game_id)

@router.get("/popular")
async def popular_games(page: int = 1):
    return await get_popular_games(page)

@router.get("/genres")
async def genres():
    return await get_genres()