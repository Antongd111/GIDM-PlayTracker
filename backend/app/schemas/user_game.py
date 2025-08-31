from pydantic import BaseModel, Field, AliasChoices
from typing import Optional
from datetime import datetime

class UserGameBase(BaseModel):
    game_rawg_id: int = Field(..., alias="gameRawgId")
    status: Optional[str] = None
    score: Optional[int] = None
    notes: Optional[str] = None

    class Config:
        from_attributes = True
        populate_by_name = True


class UserGameCreate(UserGameBase):
    # opcionalmente permitir que el cliente mande también previews
    game_title: Optional[str] = Field(None, alias="gameTitle")
    image_url: Optional[str] = Field(None, alias="imageUrl")
    release_year: Optional[int] = Field(None, alias="releaseYear")


class UserGameUpdate(BaseModel):
    status: Optional[str] = None
    score: Optional[int] = None
    notes: Optional[str] = None
    game_title: Optional[str] = Field(None, alias="gameTitle")
    image_url: Optional[str] = Field(None, alias="imageUrl")
    release_year: Optional[int] = Field(None, alias="releaseYear")
    contains_spoilers: Optional[bool] = Field(None, alias="containsSpoilers")

    class Config:
        populate_by_name = True


class UserGameOut(BaseModel):
    id: int
    user_id: int = Field(..., alias="userId")
    game_rawg_id: int = Field(..., alias="gameRawgId")

    game_title: Optional[str] = Field(None, alias="gameTitle")
    image_url: Optional[str] = Field(None, alias="imageUrl")
    release_year: Optional[int] = Field(None, alias="releaseYear")

    status: Optional[str] = None
    score: Optional[int] = None
    notes: Optional[str] = None
    added_at: datetime = Field(..., alias="addedAt")
    review_updated_at: Optional[datetime] = Field(None, alias="reviewUpdatedAt")
    contains_spoilers: Optional[bool] = Field(None, alias="containsSpoilers")

    class Config:
        from_attributes = True
        populate_by_name = True
