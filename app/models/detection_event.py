from datetime import datetime

from sqlalchemy import DateTime, Float, ForeignKey, JSON, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base
from app.models.mixins import TimestampMixin, utc_now


class DetectionEvent(Base, TimestampMixin):
    __tablename__ = "detection_events"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    analysis_job_id: Mapped[int] = mapped_column(ForeignKey("analysis_jobs.id"))
    scenario_id: Mapped[int] = mapped_column(ForeignKey("scenarios.id"))
    video_source_id: Mapped[int] = mapped_column(ForeignKey("video_sources.id"))
    event_type: Mapped[str] = mapped_column(String(30))
    severity: Mapped[str] = mapped_column(String(30), default="LOW")
    confidence_score: Mapped[float] = mapped_column(Float, default=0)
    detected_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    snapshot_url: Mapped[str | None] = mapped_column(String(500), nullable=True)
    clip_url: Mapped[str | None] = mapped_column(String(500), nullable=True)
    result_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)

    analysis_job = relationship("AnalysisJob", back_populates="detection_events")
    scenario = relationship("Scenario", back_populates="detection_events")
    video_source = relationship("VideoSource", back_populates="detection_events")
    alerts = relationship("Alert", back_populates="detection_event")
