import httpx
from fastapi import HTTPException
from app.core.config import settings

RAWG_API_BASE_URL = "https://api.rawg.io/api"

# Buscar juego por nombre
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

# Obtener detalles de un juego por ID
async def get_game_details(game_id: int):
    params = {"key": settings.RAWG_API_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{RAWG_API_BASE_URL}/games/{game_id}", params=params)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="No se pudo obtener el detalle del juego")
    return response.json()


# Obtener juegos populares
async def get_popular_games(page: int = 1):
    params = {
        "key": settings.RAWG_API_KEY,
        "ordering": "-rating",
        "page_size": 10,
        "page": page
    }
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{RAWG_API_BASE_URL}/games", params=params)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="No se pudieron obtener juegos populares")
    return response.json()


# Obtener lista de géneros
async def get_genres():
    params = {"key": settings.RAWG_API_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{RAWG_API_BASE_URL}/genres", params=params)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="No se pudieron obtener los géneros")
    return response.json()