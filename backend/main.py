from fastapi import FastAPI
from app.core.init_db import init_db

app = FastAPI()

@app.on_event("startup")
async def startup():
    await init_db()