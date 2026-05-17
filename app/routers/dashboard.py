from fastapi import APIRouter, Depends
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.deps import get_current_user
from app.models.alert import Alert
from app.models.analysis_job import AnalysisJob
from app.models.detection_event import DetectionEvent
from app.models.scenario import Scenario
from app.models.user import User
from app.models.video_source import VideoSource

router = APIRouter()


@router.get("/summary")
def dashboard_summary(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    scenario_count = db.scalar(
        select(func.count()).select_from(Scenario).where(Scenario.user_id == current_user.id)
    )
    video_count = db.scalar(
        select(func.count()).select_from(VideoSource).where(VideoSource.user_id == current_user.id)
    )
    job_count = db.scalar(
        select(func.count())
        .select_from(AnalysisJob)
        .join(Scenario)
        .where(Scenario.user_id == current_user.id)
    )
    abnormal_count = db.scalar(
        select(func.count())
        .select_from(DetectionEvent)
        .join(Scenario)
        .where(Scenario.user_id == current_user.id)
        .where(DetectionEvent.event_type == "ABNORMAL")
    )
    unread_alert_count = db.scalar(
        select(func.count())
        .select_from(Alert)
        .where(Alert.receiver_id == current_user.id)
        .where(Alert.status != "READ")
    )

    return {
        "success": True,
        "data": {
            "scenarios": scenario_count,
            "videos": video_count,
            "analysis_jobs": job_count,
            "abnormal_events": abnormal_count,
            "unread_alerts": unread_alert_count,
        },
    }
