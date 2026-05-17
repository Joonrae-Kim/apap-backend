from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.deps import get_current_user
from app.models.alert import Alert
from app.models.analysis_job import AnalysisJob
from app.models.detection_event import DetectionEvent
from app.models.scenario import Scenario
from app.models.user import User
from app.models.video_source import VideoSource
from app.schemas.analysis import AnalysisCallbackRequest, AnalysisJobCreate, AnalysisJobResponse
from app.services.ai_client import build_analysis_payload

router = APIRouter()


@router.post("/jobs")
def create_analysis_job(
    payload: AnalysisJobCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    scenario = db.get(Scenario, payload.scenario_id)
    video_source = db.get(VideoSource, payload.video_source_id)
    if not scenario or scenario.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Scenario not found")
    if not video_source or video_source.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Video source not found")

    job = AnalysisJob(
        scenario_id=scenario.id,
        video_source_id=video_source.id,
        status="PENDING",
    )
    db.add(job)
    db.commit()
    db.refresh(job)

    return {
        "success": True,
        "data": {
            "job": AnalysisJobResponse.model_validate(job),
            "ai_request_payload": build_analysis_payload(job, scenario, video_source),
        },
        "message": "Analysis job created. Send ai_request_payload to AI server.",
    }


@router.get("/jobs", response_model=list[AnalysisJobResponse])
def list_analysis_jobs(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return db.scalars(
        select(AnalysisJob)
        .join(Scenario)
        .where(Scenario.user_id == current_user.id)
        .order_by(AnalysisJob.id.desc())
    ).all()


@router.get("/jobs/{job_id}", response_model=AnalysisJobResponse)
def get_analysis_job(
    job_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    job = db.get(AnalysisJob, job_id)
    if not job or job.scenario.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Analysis job not found")
    return job


@router.post("/callback")
def receive_analysis_callback(
    payload: AnalysisCallbackRequest,
    db: Session = Depends(get_db),
):
    job = db.get(AnalysisJob, payload.job_id)
    if not job:
        raise HTTPException(status_code=404, detail="Analysis job not found")

    job.status = payload.status
    job.error_message = payload.error_message
    job.completed_at = datetime.now(timezone.utc)

    for item in payload.events:
        event = DetectionEvent(
            analysis_job_id=job.id,
            scenario_id=job.scenario_id,
            video_source_id=job.video_source_id,
            event_type=item.event_type,
            severity=item.severity,
            confidence_score=item.confidence_score,
            detected_at=item.detected_at or datetime.now(timezone.utc),
            snapshot_url=item.snapshot_url,
            clip_url=item.clip_url,
            result_json=item.result,
        )
        db.add(event)
        db.flush()

        if item.event_type == "ABNORMAL":
            db.add(
                Alert(
                    detection_event_id=event.id,
                    receiver_id=job.scenario.user_id,
                    channel="DASHBOARD",
                    status="SENT",
                    message=f"Abnormal behavior detected: severity={item.severity}",
                    sent_at=datetime.now(timezone.utc),
                )
            )

    db.commit()
    return {"success": True, "message": "Analysis result saved"}
