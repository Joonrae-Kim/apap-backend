from sqlalchemy import Boolean, Float, ForeignKey, JSON, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base
from app.models.mixins import TimestampMixin


class Scenario(Base, TimestampMixin):
    __tablename__ = "scenarios"

    id: Mapped[int] = mapped_column(primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))
    name: Mapped[str] = mapped_column(String(100), index=True)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    target_location: Mapped[str | None] = mapped_column(String(150), nullable=True)
    behavior_text: Mapped[str] = mapped_column(Text)
    conditions_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    threshold: Mapped[float] = mapped_column(Float, default=0.8)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    user = relationship("User", back_populates="scenarios")
    analysis_jobs = relationship("AnalysisJob", back_populates="scenario")
    detection_events = relationship("DetectionEvent", back_populates="scenario")
