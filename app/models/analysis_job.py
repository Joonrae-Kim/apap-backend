from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base
from app.models.mixins import TimestampMixin, utc_now


class AnalysisJob(Base, TimestampMixin):
    __tablename__ = "analysis_jobs"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    scenario_id: Mapped[int] = mapped_column(ForeignKey("scenarios.id"))
    video_source_id: Mapped[int] = mapped_column(ForeignKey("video_sources.id"))
    status: Mapped[str] = mapped_column(String(30), default="PENDING")
    requested_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    error_message: Mapped[str | None] = mapped_column(Text, nullable=True)

    scenario = relationship("Scenario", back_populates="analysis_jobs")
    video_source = relationship("VideoSource", back_populates="analysis_jobs")
    detection_events = relationship("DetectionEvent", back_populates="analysis_job")
