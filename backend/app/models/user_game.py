from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Boolean, UniqueConstraint
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
    review_updated_at = Column(DateTime(timezone=True), nullable=True)
    contains_spoilers = Column(Boolean, nullable=False, default=False, server_default="false")

    __table_args__ = (
        UniqueConstraint("user_id", "game_rawg_id", name="uq_user_games_user_game"),
    )

    user = relationship("User", back_populates="games")