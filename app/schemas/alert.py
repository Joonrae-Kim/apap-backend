from datetime import datetime

from pydantic import BaseModel


class AlertResponse(BaseModel):
    id: int
    detection_event_id: int
    receiver_id: int
    channel: str
    status: str
    message: str
    sent_at: datetime | None = None
    read_at: datetime | None = None

    model_config = {"from_attributes": True}
