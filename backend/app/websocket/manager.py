from collections import defaultdict
from fastapi import WebSocket

class ConnectionManager:
    def __init__(self):
        self.connections = defaultdict(set)

    async def connect(self, user_id: str, websocket: WebSocket) -> None:
        await websocket.accept()
        self.connections[user_id].add(websocket)

    def disconnect(self, user_id: str, websocket: WebSocket) -> None:
        self.connections[user_id].discard(websocket)
        if not self.connections[user_id]:
            self.connections.pop(user_id, None)

    async def send_to_user(self, user_id: str, message: dict) -> None:
        for websocket in list(self.connections.get(user_id, ())):
            await websocket.send_json(message)

    async def broadcast(self, room: str, message: dict) -> None:
        for websocket in list(self.connections.get(room, ())):
            try:
                await websocket.send_json(message)
            except Exception:
                self.disconnect(room, websocket)

manager = ConnectionManager()
