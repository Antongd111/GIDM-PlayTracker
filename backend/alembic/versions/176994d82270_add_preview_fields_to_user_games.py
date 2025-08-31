"""add preview fields to user_games

Revision ID: 176994d82270
Revises: 0285c4f1dcd0
Create Date: 2025-08-31 12:35:52.281722

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '176994d82270'
down_revision: Union[str, Sequence[str], None] = '0285c4f1dcd0'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade():
    op.add_column("user_games", sa.Column("game_title", sa.String(), nullable=True))
    op.add_column("user_games", sa.Column("image_url", sa.String(), nullable=True))
    op.add_column("user_games", sa.Column("release_year", sa.Integer(), nullable=True))

def downgrade():
    op.drop_column("user_games", "release_year")
    op.drop_column("user_games", "image_url")
    op.drop_column("user_games", "game_title")