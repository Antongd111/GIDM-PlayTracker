"""add game_catalog table

Revision ID: 0285c4f1dcd0
Revises: 4a8509f51215
Create Date: 2025-08-29 11:38:45.338859

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision: str = '0285c4f1dcd0'
down_revision: Union[str, Sequence[str], None] = '4a8509f51215'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # --- CREA game_catalog ---
    op.create_table(
        "game_catalog",
        sa.Column("game_rawg_id", sa.BigInteger(), primary_key=True, index=True),
        sa.Column("name", sa.String(), nullable=False),
        sa.Column("genres", sa.String(), nullable=True),
        sa.Column("tags", sa.String(), nullable=True),
        sa.Column("platforms", sa.String(), nullable=True),
        sa.Column("metacritic", sa.Integer(), nullable=True),
        sa.Column("rating", sa.Float(), nullable=True),
    )

    # --- (Opcional) Cambios de tipo a BigInteger para RAWG IDs ---
    # Quita estas dos alter_column si NO quieres cambiar los tipos ahora.
    op.alter_column(
        "review_likes", "review_game_rawg_id",
        existing_type=sa.INTEGER(), type_=sa.BigInteger(), existing_nullable=False
    )
    op.alter_column(
        "users", "favorite_rawg_game_id",
        existing_type=sa.INTEGER(), type_=sa.BigInteger(), existing_nullable=True
    )

    # Importante: NO borramos friendships ni sus índices.
    # Si Alembic había generado drop_index para review_likes, NO lo hacemos aquí.


def downgrade() -> None:
    """Downgrade schema."""
    # Revertir tipos si los cambiaste arriba
    op.alter_column(
        "users", "favorite_rawg_game_id",
        existing_type=sa.BigInteger(), type_=sa.INTEGER(), existing_nullable=True
    )
    op.alter_column(
        "review_likes", "review_game_rawg_id",
        existing_type=sa.BigInteger(), type_=sa.INTEGER(), existing_nullable=False
    )

    # Eliminar game_catalog
    op.drop_table("game_catalog")
