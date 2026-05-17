from pydantic import BaseModel


class VideoSourceCreate(BaseModel):
    type: str = "UPLOAD"
    name: str
    source_url: str


class VideoSourceUpdate(BaseModel):
    name: str | None = None
    source_url: str | None = None
    status: str | None = None


class VideoSourceResponse(BaseModel):
    id: int
    user_id: int
    type: str
    name: str
    source_url: str
    status: str

    model_config = {"from_attributes": True}
