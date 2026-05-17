from datetime import datetime

from pydantic import BaseModel


class AnalysisJobCreate(BaseModel):
    scenario_id: int
    video_source_id: int


class AnalysisJobResponse(BaseModel):
    id: int
    scenario_id: int
    video_source_id: int
    status: str
    requested_at: datetime
    completed_at: datetime | None = None
    error_message: str | None = None

    model_config = {"from_attributes": True}


class DetectionEventCallback(BaseModel):
    event_type: str
    severity: str = "LOW"
    confidence_score: float
    detected_at: datetime | None = None
    snapshot_url: str | None = None
    clip_url: str | None = None
    result: dict | None = None


class AnalysisCallbackRequest(BaseModel):
    job_id: int
    status: str
    error_message: str | None = None
    events: list[DetectionEventCallback] = []
