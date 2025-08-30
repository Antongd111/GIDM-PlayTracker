# backend/scripts/seed_catalog.py
import asyncio
from typing import Any
import sys
from pathlib import Path
sys.path.append(str(Path(__file__).resolve().parents[1]))

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

import app.core.database as dbmod
SessionFactory = (
    getattr(dbmod, "AsyncSessionLocal", None)
    or getattr(dbmod, "async_session_maker", None)
    or getattr(dbmod, "SessionLocal", None)
)
if SessionFactory is None:
    raise ImportError("No encuentro tu factory de sesión asíncrona.")

from app.core.rawg import get_popular_games, get_game_details
from app.models.game_catalog import GameCatalog


def get_id_from_item(item: Any) -> int | None:
    if isinstance(item, dict) and "id" in item:
        try: return int(item["id"])
        except: return None
    _id = getattr(item, "id", None)
    if _id is not None:
        try: return int(_id)
        except: return None
    try: return int(item)
    except: return None

def _to_name_list(seq: Any) -> list[str]:
    if not seq: return []
    out: list[str] = []
    for it in seq:
        if isinstance(it, dict):
            n = it.get("name")
            if not n and "platform" in it and isinstance(it["platform"], dict):
                n = it["platform"].get("name")
            if n: out.append(str(n)); continue
        n = getattr(it, "name", None)
        if n: out.append(str(n)); continue
        plat = getattr(it, "platform", None)
        if plat:
            pn = getattr(plat, "name", None)
            if pn: out.append(str(pn)); continue
        if isinstance(it, str): out.append(it)
    return [s.strip() for s in out if s and s.strip()]

def _normalize_detail(detail: Any) -> dict:
    if isinstance(detail, dict):
        data = detail
    else:
        try:
            from pydantic import BaseModel  # type: ignore
            if isinstance(detail, BaseModel):
                try: data = detail.model_dump()
                except: data = detail.dict()
            else:
                data = {
                    "name": getattr(detail, "name", None),
                    "genres": getattr(detail, "genres", None),
                    "tags": getattr(detail, "tags", None),
                    "platforms": getattr(detail, "platforms", None),
                    "metacritic": getattr(detail, "metacritic", None),
                    "rating": getattr(detail, "rating", None),
                }
        except:
            data = {
                "name": getattr(detail, "name", None),
                "genres": getattr(detail, "genres", None),
                "tags": getattr(detail, "tags", None),
                "platforms": getattr(detail, "platforms", None),
                "metacritic": getattr(detail, "metacritic", None),
                "rating": getattr(detail, "rating", None),
            }
    return {
        "name": (data.get("name") if isinstance(data, dict) else "") or "",
        "genres": _to_name_list(data.get("genres") if isinstance(data, dict) else []),
        "tags": _to_name_list(data.get("tags") if isinstance(data, dict) else [])[:30],
        "platforms": _to_name_list(data.get("platforms") if isinstance(data, dict) else []),
        "metacritic": data.get("metacritic") if isinstance(data, dict) else None,
        "rating": data.get("rating") if isinstance(data, dict) else None,
    }

async def upsert_game_catalog_async(
    db: AsyncSession, *, rawg_id: int, name: str,
    genres: list[str], tags: list[str], platforms: list[str],
    metacritic: int | None, rating: float | None
) -> GameCatalog:
    res = await db.execute(select(GameCatalog).where(GameCatalog.game_rawg_id == rawg_id))
    g = res.scalar_one_or_none()
    genres_str = ";".join(genres or [])
    tags_str = ";".join(tags or [])
    plats_str = ";".join(platforms or [])
    if g is None:
        g = GameCatalog(
            game_rawg_id=rawg_id, name=name,
            genres=genres_str, tags=tags_str, platforms=plats_str,
            metacritic=metacritic, rating=rating
        )
        db.add(g)
    else:
        g.name = name; g.genres = genres_str; g.tags = tags_str
        g.platforms = plats_str; g.metacritic = metacritic; g.rating = rating
    await db.commit(); await db.refresh(g)
    return g

# ---- seeding ----
async def seed_once(page: int, page_size: int = 40):
    print(f"Sembrando página {page} (page_size={page_size})…")
    # Si tu get_popular_games no acepta page_size, hacemos fallback:
    try:
        popular_list = await get_popular_games(page, page_size)
    except TypeError:
        popular_list = await get_popular_games(page)
    results = popular_list

    async with SessionFactory() as db:
        for item in results:
            rawg_id = get_id_from_item(item)
            if not rawg_id:
                print(f" -> item sin id legible: {item}")
                continue
            try:
                detail = await get_game_details(rawg_id)
                d = _normalize_detail(detail)
                await upsert_game_catalog_async(
                    db=db,
                    rawg_id=int(rawg_id),
                    name=d["name"] or "",
                    genres=d["genres"],
                    tags=d["tags"],
                    platforms=d["platforms"],
                    metacritic=d["metacritic"],
                    rating=d["rating"],
                )
            except Exception as e:
                print(f" -> fallo con id {rawg_id}: {e}")
                continue
            await asyncio.sleep(0.15)  # cuida el rate limit

async def main():
    # 8 páginas * 40 = 320 (~300)
    for page in range(1, 9):
        await seed_once(page, page_size=40)

if __name__ == "__main__":
    asyncio.run(main())
