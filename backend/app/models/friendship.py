# app/models/friendship.py
from __future__ import annotations
import enum
from datetime import datetime
from typing import Optional

from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy import (
    Integer, ForeignKey, DateTime, func,
    CheckConstraint, UniqueConstraint, Enum as SAEnum
)

from app.core.database import Base


class FriendshipStatus(str, enum.Enum):
    pending  = "pending"
    accepted = "accepted"
    declined = "declined"
    blocked  = "blocked"


class Friendship(Base):
    __tablename__ = "friendships"

    id: Mapped[int] = mapped_column(primary_key=True)

    # Siempre guardamos el par ordenado: user_id_a < user_id_b
    user_id_a: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))
    user_id_b: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))

    requester_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))

    status: Mapped[FriendshipStatus] = mapped_column(
        SAEnum(FriendshipStatus, name="friendship_status"),
        default=FriendshipStatus.pending,
        nullable=False,
    )

    requested_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now()
    )
    responded_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True), nullable=True
    )

    blocker_id: Mapped[Optional[int]] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"), nullable=True
    )

    __table_args__ = (
        # Reglas de coherencia
        CheckConstraint("user_id_a <> user_id_b", name="chk_distinct_users"),
        CheckConstraint(
            "(requester_id = user_id_a) OR (requester_id = user_id_b)",
            name="chk_requester_in_pair",
        ),
        CheckConstraint(
            "(status <> 'blocked') OR (blocker_id IS NOT NULL)",
            name="chk_blocker_when_blocked",
        ),
        # Par siempre ordenado y único
        CheckConstraint("user_id_a < user_id_b", name="chk_pair_sorted"),
        UniqueConstraint("user_id_a", "user_id_b", name="uq_friendships_pair_sorted"),
    )
