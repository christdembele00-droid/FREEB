from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

import cloudinary.uploader

from app.auth.dependencies import get_current_user
from app.core.config import settings
from app.db.models import MediaAsset, User
from app.db.session import get_db
from app.media.cloudinary_service import configure_cloudinary

router = APIRouter(prefix="/media", tags=["media"])

@router.post("/upload")
async def upload_media(
    file: UploadFile = File(...),
    kind: str = Form("IMAGE"),
    folder: str = Form("freeb/dev"),
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if not file.filename:
        raise HTTPException(400, "Missing file name")

    configure_cloudinary(
        settings.cloudinary_cloud_name,
        settings.cloudinary_api_key,
        settings.cloudinary_api_secret,
    )

    content_type = file.content_type or "application/octet-stream"
    if kind == "IMAGE" and not content_type.startswith("image/"):
        raise HTTPException(415, "Expected image")
    if kind == "VIDEO" and not content_type.startswith("video/"):
        raise HTTPException(415, "Expected video")

    result = cloudinary.uploader.upload(
        file.file,
        folder=folder,
        resource_type="auto",
        overwrite=False,
    )

    asset = MediaAsset(
        user_id=user.id,
        public_id=result["public_id"],
        secure_url=result["secure_url"],
        resource_type=result.get("resource_type", "image"),
        format=result.get("format"),
        bytes=result.get("bytes"),
        width=result.get("width"),
        height=result.get("height"),
        duration=result.get("duration"),
    )
    db.add(asset)
    await db.commit()
    await db.refresh(asset)

    return {
        "id": asset.id,
        "public_id": asset.public_id,
        "secure_url": asset.secure_url,
        "resource_type": asset.resource_type,
        "format": asset.format,
        "bytes": asset.bytes,
        "width": asset.width,
        "height": asset.height,
        "duration": asset.duration,
    }
