from app.core.database import engine, Base
from app.models import user, user_game

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)