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

class UserOut(UserBase):
    id: int

    class Config:
        orm_mode = True