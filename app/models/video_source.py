from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base
from app.models.mixins import TimestampMixin


class VideoSource(Base, TimestampMixin):
    __tablename__ = "video_sources"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))
    type: Mapped[str] = mapped_column(String(30), default="UPLOAD")
    name: Mapped[str] = mapped_column(String(120))
    source_url: Mapped[str] = mapped_column(String(500))
    status: Mapped[str] = mapped_column(String(30), default="READY")

    user = relationship("User", back_populates="video_sources")
    analysis_jobs = relationship("AnalysisJob", back_populates="video_source")
    detection_events = relationship("DetectionEvent", back_populates="video_source")
