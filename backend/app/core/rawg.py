import httpx
from fastapi import HTTPException
from app.core.config import settings

RAWG_API_BASE_URL = "https://api.rawg.io/api"

async def search_games(query: str):
    params = {
        "key": settings.RAWG_API_KEY,
        "search": query,
        "page_size": 10
    }

    async with httpx.AsyncClient() as client:
        response = await client.get(f"{RAWG_API_BASE_URL}/games", params=params)

    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="Error al conectar con RAWG API")

    return response.json()