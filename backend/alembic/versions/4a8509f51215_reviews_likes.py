"""reviews + likes

Revision ID: 4a8509f51215
Revises: 6618294c04ad
Create Date: 2025-08-24 18:07:16.369756

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

# revision identifiers, used by Alembic.
revision: str = '4a8509f51215'
down_revision: Union[str, Sequence[str], None] = '6618294c04ad'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # --- NO borres friendships aquí ---
    # op.drop_table('friendships')  # <- elimínalo

    # 1) Alinea tipos o cambia review_likes a INTEGER.
    #    Opción simple: mantener user_games.game_rawg_id como INTEGER
    #    y usar INTEGER también en review_likes. (No tocamos user_games.)
    #    Si prefieres BIGINT en todo, avísame y te paso la versión alterando user_games primero.

    # 2) Columnas nuevas en user_games
    op.add_column('user_games', sa.Column('review_updated_at', sa.DateTime(timezone=True), nullable=True))
    op.add_column('user_games', sa.Column('contains_spoilers', sa.Boolean(), server_default=sa.text('false'), nullable=False))

    # 3) UNIQUE necesaria para la FK compuesta
    op.create_unique_constraint('uq_user_games_user_game', 'user_games', ['user_id', 'game_rawg_id'])

    # 4) Tabla review_likes (con tipos que COINCIDAN con user_games)
    op.create_table(
        'review_likes',
        sa.Column('review_user_id', sa.Integer(), nullable=False),
        sa.Column('review_game_rawg_id', sa.Integer(), nullable=False),  # <--- INTEGER para cuadrar con user_games
        sa.Column('liker_user_id', sa.Integer(), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('NOW()'), nullable=False),
        sa.PrimaryKeyConstraint('review_user_id', 'review_game_rawg_id', 'liker_user_id', name='pk_review_likes'),
        sa.ForeignKeyConstraint(['review_user_id'], ['users.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['liker_user_id'], ['users.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(
            ['review_user_id', 'review_game_rawg_id'],
            ['user_games.user_id', 'user_games.game_rawg_id'],
            name='fk_like_review_pair',
            ondelete='CASCADE'
        ),
    )
    op.create_index('idx_review_likes_game', 'review_likes', ['review_game_rawg_id'])
    op.create_index('idx_review_likes_liker', 'review_likes', ['liker_user_id'])

    # ### end Alembic commands ###


def downgrade() -> None:
    op.drop_index('idx_review_likes_liker', table_name='review_likes')
    op.drop_index('idx_review_likes_game', table_name='review_likes')
    op.drop_table('review_likes')

    op.drop_constraint('uq_user_games_user_game', 'user_games', type_='unique')
    op.drop_column('user_games', 'contains_spoilers')
    op.drop_column('user_games', 'review_updated_at')
    # ### end Alembic commands ###
