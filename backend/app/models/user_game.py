from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from app.core.database import Base

class UserGame(Base):
    __tablename__ = "user_games"

    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)

    game_rawg_id = Column(Integer, nullable=False)
    status = Column(String)        # "jugando", "terminado", etc.
    score = Column(Integer)        # nota del usuario
    notes = Column(String)         # comentario personal
    added_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="games")