import time

import cloudinary
import cloudinary.utils


def configure_cloudinary(cloud_name: str, api_key: str, api_secret: str) -> None:
    cloudinary.config(
        cloud_name=cloud_name,
        api_key=api_key,
        api_secret=api_secret,
        secure=True,
    )


def signed_upload_parameters(folder: str) -> dict:
    timestamp = int(time.time())
    params = {"folder": folder, "timestamp": timestamp}
    signature = cloudinary.utils.api_sign_request(
        params, cloudinary.config().api_secret
    )
    return {"params": params, "signature": signature}
