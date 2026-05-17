from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "APAP Backend"
    env: str = "local"
    secret_key: str = "change-this-secret-key"
    access_token_expire_minutes: int = 1440
    database_url: str = "sqlite:///./apap.db"
    ai_server_url: str = "http://127.0.0.1:9000"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
