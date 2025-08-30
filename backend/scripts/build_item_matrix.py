# backend/scripts/build_item_matrix.py
import asyncio
import sys
from pathlib import Path

# añade /backend al PYTHONPATH
sys.path.append(str(Path(__file__).resolve().parents[1]))

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from scipy import sparse
import joblib

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.game_catalog import GameCatalog

# 👇 importa tu sessionmaker asíncrono (ajusta el nombre si en tu proyecto es distinto)
# Suele llamarse AsyncSessionLocal, async_session_maker o similar.
from app.core.database import SessionLocal  # <- si en tu proyecto se llama diferente, cámbialo


def _norm(csv_str: str | None) -> list[str]:
    if not csv_str:
        return []
    return [x.strip().lower().replace(" ", "_") for x in csv_str.split(";") if x.strip()]


def _tokens(row: GameCatalog) -> str:
    toks = []
    toks += [f"genre:{g}" for g in _norm(row.genres)]
    toks += [f"tag:{t}" for t in _norm(row.tags)]
    toks += [f"platform:{p}" for p in _norm(row.platforms)]
    return " ".join(sorted(set(toks)))


async def load_catalog_df() -> pd.DataFrame:
    async with SessionLocal() as db:  # type: AsyncSession
        result = await db.execute(select(GameCatalog))
        rows = result.scalars().all()
    data = [{"game_rawg_id": int(r.game_rawg_id), "tokens": _tokens(r)} for r in rows]
    return pd.DataFrame(data)


async def main():
    df = await load_catalog_df()
    if df.empty:
        print("Catálogo vacío. Primero llena game_catalog llamando a /rawg/games/{id}.")
        return

    vec = TfidfVectorizer(min_df=1)
    X = vec.fit_transform(df["tokens"])

    out = Path("model_store")
    out.mkdir(exist_ok=True)
    joblib.dump(vec, out / "tfidf_vectorizer.joblib")
    sparse.save_npz(out / "item_matrix.npz", X)
    df[["game_rawg_id"]].to_csv(out / "item_ids.csv", index=False)

    print(f"OK. {X.shape[0]} juegos, {X.shape[1]} tokens. Artefactos en {out.resolve()}")


if __name__ == "__main__":
    asyncio.run(main())
