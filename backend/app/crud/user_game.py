from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.user_game import UserGame
from app.schemas.user_game import UserGameCreate, UserGameUpdate

async def get_user_games(db: AsyncSession, user_id: int):
    result = await db.execute(select(UserGame).where(UserGame.user_id == user_id))
    return result.scalars().all()

async def get_user_game(db: AsyncSession, user_id: int, game_id: int):
    result = await db.execute(
        select(UserGame).where(UserGame.user_id == user_id, UserGame.id == game_id)
    )
    return result.scalar_one_or_none()

async def create_user_game(db: AsyncSession, user_id: int, data: UserGameCreate):
    game = UserGame(**data.dict(), user_id=user_id)
    db.add(game)
    await db.commit()
    await db.refresh(game)
    return game

async def update_user_game(db: AsyncSession, user_id: int, game_id: int, data: UserGameUpdate):
    game = await get_user_game(db, user_id, game_id)
    if not game:
        return None

    for field, value in data.dict(exclude_unset=True).items():
        setattr(game, field, value)

    await db.commit()
    await db.refresh(game)
    return game

async def delete_user_game(db: AsyncSession, user_id: int, game_id: int):
    game = await get_user_game(db, user_id, game_id)
    if not game:
        return None

    await db.delete(game)
    await db.commit()
    return game