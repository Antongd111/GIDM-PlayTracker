from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class UserGameBase(BaseModel):
    game_rawg_id: int
    status: Optional[str] = None
    score: Optional[int] = None
    notes: Optional[str] = None

class UserGameCreate(UserGameBase):
    pass

class UserGameUpdate(BaseModel):
    status: Optional[str] = None
    score: Optional[int] = None
    notes: Optional[str] = None

class UserGameOut(UserGameBase):
    id: int
    user_id: int
    added_at: datetime

    class Config:
        from_attributes = True