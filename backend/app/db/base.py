import os

from sqlalchemy import MetaData
from sqlalchemy.orm import DeclarativeBase


schema = os.getenv("FREEB_DB_SCHEMA") or None


class Base(DeclarativeBase):
    metadata = MetaData(schema=schema)
