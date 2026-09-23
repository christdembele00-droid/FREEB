from firebase_admin import messaging

def send_push(tokens: list[str], title: str, body: str, data: dict[str, str] | None = None) -> None:
    if not tokens:
        return
    for token in tokens:
        message = messaging.Message(
            token=token,
            notification=messaging.Notification(title=title, body=body),
            data=data or {},
        )
        try:
            messaging.send(message)
        except Exception:
            continue
