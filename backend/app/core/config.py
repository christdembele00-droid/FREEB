from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://freeb:freeb@localhost:5432/freeb"
    firebase_project_id: str = ""
    firebase_client_email: str = ""
    firebase_private_key: str = ""
    cloudinary_cloud_name: str = ""
    cloudinary_api_key: str = ""
    cloudinary_api_secret: str = ""
    allowed_origins: str = "http://localhost"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

settings = Settings()
