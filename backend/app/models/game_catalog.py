from sqlalchemy import Column, BigInteger, Integer, String, Float
from sqlalchemy.orm import relationship
from ..core.database import Base

class GameCatalog(Base):
    __tablename__ = "game_catalog"

    game_rawg_id = Column(BigInteger, primary_key=True, index=True)
    name = Column(String, nullable=False)

    # listas serializadas por ';'
    genres = Column(String, nullable=True)      # "Action;RPG;Indie"
    tags = Column(String, nullable=True)        # "Pixel Graphics;Souls-like"
    platforms = Column(String, nullable=True)   # "PC;PlayStation 5"

    # opcional, para popularidad / cold-start
    metacritic = Column(Integer, nullable=True)
    rating = Column(Float, nullable=True)       # rating global RAWG si lo usas
