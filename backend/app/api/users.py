from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List
from app.core.database import SessionLocal
from app.schemas.user import UserOut, UserCreate, UserUpdate
from app.crud import user as crud_user
from app.core.dependencies import get_current_user
from app.models.user import User


router = APIRouter(prefix="/users", tags=["users"])

async def get_db():
    async with SessionLocal() as session:
        yield session

@router.get("/", response_model=List[UserOut])
async def read_users(db: AsyncSession = Depends(get_db)):
    return await crud_user.get_users(db)


@router.get("/me", response_model=UserOut)
async def get_me(current_user: User = Depends(get_current_user)):
    return current_user

@router.get("/{user_id}", response_model=UserOut)
async def read_user(user_id: int, db: AsyncSession = Depends(get_db)):
    user = await crud_user.get_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return user

@router.post("/", response_model=UserOut, status_code=201)
async def create_user(user: UserCreate, db: AsyncSession = Depends(get_db)):
    return await crud_user.create_user(db, user)

@router.put("/{user_id}", response_model=UserOut)
async def update_user(user_id: int, user_update: UserUpdate, db: AsyncSession = Depends(get_db)):
    updated = await crud_user.update_user(db, user_id, user_update)
    if not updated:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return updated

@router.delete("/{user_id}", status_code=204)
async def delete_user(user_id: int, db: AsyncSession = Depends(get_db)):
    deleted = await crud_user.delete_user(db, user_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

@router.get("/search/", response_model=List[UserOut])
async def search_users(query: str, db: AsyncSession = Depends(get_db)):
    users = await crud_user.search_users(db, query)
    return users