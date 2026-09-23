import asyncio

from app.services.cleanup import cleanup_expired

if __name__ == "__main__":
    asyncio.run(cleanup_expired())
