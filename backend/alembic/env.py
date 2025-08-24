import asyncio
import os, sys
from logging.config import fileConfig

import app.models.review_like

from alembic import context
from sqlalchemy import pool
from sqlalchemy.ext.asyncio import async_engine_from_config

# --- Asegura que 'app/' esté en sys.path ---
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
sys.path.append(BASE_DIR)

# --- Tu config / Base / modelos ---
from app.core.config import settings
from app.core.database import Base  # o donde tengas tu Base

# IMPORTA TODOS LOS MODELOS para que Alembic los vea
import app.models.user           # noqa: F401
import app.models.user_game      # noqa: F401
# import app.models.friendship   # noqa si lo tienes
# import app.models.review_like  # lo añadirás luego cuando creemos la tabla de likes

config = context.config

# Logging (opcional)
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata

def run_migrations_offline() -> None:
    url = settings.DATABASE_URL
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
        compare_type=True,
        compare_server_default=True,
    )
    with context.begin_transaction():
        context.run_migrations()

def do_run_migrations(connection) -> None:
    context.configure(
        connection=connection,
        target_metadata=target_metadata,
        compare_type=True,
        compare_server_default=True,
    )
    with context.begin_transaction():
        context.run_migrations()

async def run_migrations_online() -> None:
    # Inyecta la URL aquí (desde .env)
    config.set_main_option("sqlalchemy.url", settings.DATABASE_URL)

    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
        future=True,
    )

    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)

    await connectable.dispose()

if context.is_offline_mode():
    run_migrations_offline()
else:
    asyncio.run(run_migrations_online())
