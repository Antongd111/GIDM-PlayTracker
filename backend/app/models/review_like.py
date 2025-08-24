from sqlalchemy import Column, Integer, DateTime, ForeignKey, ForeignKeyConstraint, BigInteger, func
from app.core.database import Base

class ReviewLike(Base):
    __tablename__ = "review_likes"

    review_user_id      = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    review_game_rawg_id = Column(BigInteger, primary_key=True)
    liker_user_id       = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    created_at          = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    __table_args__ = (
        ForeignKeyConstraint(
            ["review_user_id", "review_game_rawg_id"],
            ["user_games.user_id", "user_games.game_rawg_id"],
            ondelete="CASCADE",
            name="fk_like_review_pair",
        ),
    )
