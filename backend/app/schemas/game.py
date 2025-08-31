from pydantic import BaseModel
from typing import List, Optional

class SimilarGame(BaseModel):
    id: int
    title: str
    imageUrl: str

class GameDetailResponse(BaseModel):
    id: int
    title: str
    description: str
    releaseDate: Optional[str]
    imageUrl: str
    rating: float
    platforms: List[str]
    genres: List[str]
    developers: List[str]
    publishers: List[str]
    tags: List[str]
    esrbRating: Optional[str]
    metacriticScore: Optional[int]
    metacriticUrl: Optional[str]
    website: Optional[str]
    screenshots: List[str]
    videos: List[str]
    similarGames: List[SimilarGame]

class GamePreview(BaseModel):
    id: int
    title: str
    imageUrl:str
    year: int

class Recommendations(BaseModel):
    user_id: int
    items: List[GamePreview]