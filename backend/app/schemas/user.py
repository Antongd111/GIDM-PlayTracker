from pydantic import BaseModel, EmailStr
from typing import Optional

class UserBase(BaseModel):
    email: EmailStr
    username: str
    status: Optional[str] = "Disponible"
    avatar_url: Optional[str] = None

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    username: Optional[str] = None
    status: Optional[str] = None
    avatar_url: Optional[str] = None
    favorite_rawg_game_id: Optional[int] = None

class UserOut(UserBase):
    id: int
    favorite_rawg_game_id: Optional[int] = None

    class Config:
        orm_mode = True

class FavoriteUpdate(BaseModel):
    favorite_rawg_game_id: Optional[int] = None