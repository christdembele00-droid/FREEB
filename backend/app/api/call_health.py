from fastapi import APIRouter

router = APIRouter(prefix="/calls", tags=["calls"])

@router.get("/health")
async def health():
    return {"service": "calls", "status": "ok"}
