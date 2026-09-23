from fastapi import APIRouter
from sqlalchemy import text

from app.core.config import settings
from app.db.session import SessionLocal

router = APIRouter(tags=["health"])


@router.get("/health")
async def health():
    database = "ok"
    try:
        async with SessionLocal() as db:
            await db.execute(text("SELECT 1"))
    except Exception:
        database = "error"

    return {
        "status": "ok" if database == "ok" else "degraded",
        "database": database,
        "firebase_configured": settings.firebase_configured,
        "cloudinary_configured": bool(
            settings.cloudinary_cloud_name
            and settings.cloudinary_api_key
            and settings.cloudinary_api_secret
        ),
    }


@router.get("/health/ready")
async def readiness():
    try:
        async with SessionLocal() as db:
            await db.execute(text("SELECT 1"))
    except Exception:
        return {"status": "degraded", "database": "unavailable", "service": "up"}
    return {"status": "ready", "database": "ok", "service": "up"}
