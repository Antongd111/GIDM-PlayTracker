from fastapi import APIRouter, Query
from app.core.rawg import search_games

router = APIRouter()

@router.get("/games/search")
async def search(query: str = Query(..., min_length=1)):
    return await search_games(query)