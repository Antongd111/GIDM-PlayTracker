import httpx
from fastapi import HTTPException
from app.core.config import settings

RAWG_API_BASE_URL = "https://api.rawg.io/api"

# Función para mapear los datos al formato del frontend (reducido)
def format_game(game):
    return {
        "id": game["id"],
        "title": game["name"],
        "year": int(game["released"][:4]) if game.get("released") else 0,
        "imageUrl": game.get("background_image", ""),
        "rating": game.get("rating", 0)
    }

# Función para mapear los datos al formato del frontend (extendido)
def format_game_detail(game, screenshots, trailers, similar_games):
    return {
        "id": game["id"],
        "title": game["name"],
        "description": game.get("description_raw", ""),
        "releaseDate": game.get("released"),
        "imageUrl": game.get("background_image", ""),
        "rating": game.get("rating", 0),
        "platforms": [p["platform"]["name"] for p in game.get("platforms", [])],
        "genres": [g["name"] for g in game.get("genres", [])],
        "developers": [d["name"] for d in game.get("developers", [])],
        "publishers": [p["name"] for p in game.get("publishers", [])],
        "tags": [t["name"] for t in game.get("tags", [])],
        "esrbRating": game["esrb_rating"]["name"] if game.get("esrb_rating") else None,
        "metacriticScore": game.get("metacritic"),
        "metacriticUrl": game.get("metacritic_url"),
        "website": game.get("website"),
        "screenshots": [s["image"] for s in screenshots.get("results", [])],
        "videos": [v["data"]["480"] for v in trailers.get("results", [])],
        "similarGames": [
            {"id": g["id"], "title": g["name"], "imageUrl": g["background_image"]}
            for g in similar_games.get("results", [])
        ]
    }

# Buscar juegos por nombre
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

    data = response.json()
    return [format_game(game) for game in data.get("results", [])]

# Obtener detalles de un juego por ID
async def get_game_details(game_id: int):
    params = {"key": settings.RAWG_API_KEY}
    async with httpx.AsyncClient() as client:
        # Juego principal
        response = await client.get(f"{RAWG_API_BASE_URL}/games/{game_id}", params=params)
        if response.status_code != 200:
            raise HTTPException(status_code=500, detail="No se pudo obtener el detalle del juego")
        game = response.json()

        # Screenshots
        screenshots_resp = await client.get(f"{RAWG_API_BASE_URL}/games/{game_id}/screenshots", params=params)
        screenshots = screenshots_resp.json() if screenshots_resp.status_code == 200 else {"results": []}

        # Trailers
        trailers_resp = await client.get(f"{RAWG_API_BASE_URL}/games/{game_id}/movies", params=params)
        trailers = trailers_resp.json() if trailers_resp.status_code == 200 else {"results": []}

        # Juegos similares
        similar_resp = await client.get(f"{RAWG_API_BASE_URL}/games/{game_id}/suggested", params=params)
        similar_games = similar_resp.json() if similar_resp.status_code == 200 else {"results": []}

    return format_game_detail(game, screenshots, trailers, similar_games)


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

    data = response.json()
    return [format_game(game) for game in data.get("results", [])]

# Obtener lista de géneros
async def get_genres():
    params = {"key": settings.RAWG_API_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{RAWG_API_BASE_URL}/genres", params=params)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="No se pudieron obtener los géneros")
    return response.json()
