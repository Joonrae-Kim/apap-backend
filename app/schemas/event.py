from datetime import datetime

from pydantic import BaseModel


class DetectionEventResponse(BaseModel):
    id: int
    analysis_job_id: int
    scenario_id: int
    video_source_id: int
    event_type: str
    severity: str
    confidence_score: float
    detected_at: datetime
    snapshot_url: str | None = None
    clip_url: str | None = None
    result_json: dict | None = None

    model_config = {"from_attributes": True}
