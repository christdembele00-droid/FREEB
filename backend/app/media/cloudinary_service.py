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
    params = {"folder": folder}
    signature, timestamp = cloudinary.utils.api_sign_request(
        params, cloudinary.config().api_secret
    ), None
    return {"params": params, "signature": signature, "timestamp": timestamp}
