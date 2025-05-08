from fastapi import FastAPI
from app.core.init_db import init_db
from app.api import users, user_games

app = FastAPI()

@app.on_event("startup")
async def startup():
    await init_db()

app.include_router(users.router)
app.include_router(user_games.router)

@app.get("/")
def root():
    return {"message": "PlayTracker API"}