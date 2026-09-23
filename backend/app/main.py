from fastapi import FastAPI
from app.api.health import router as health_router

app = FastAPI(title="FREEB API", version="0.1.0")
app.include_router(health_router, prefix="/v1")

@app.get("/")
async def root():
    return {"service": "FREEB API", "status": "ok"}
