from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.deps import get_current_user
from app.models.detection_event import DetectionEvent
from app.models.scenario import Scenario
from app.models.user import User
from app.schemas.event import DetectionEventResponse

router = APIRouter()


@router.get("", response_model=list[DetectionEventResponse])
def list_events(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return db.scalars(
        select(DetectionEvent)
        .join(Scenario)
        .where(Scenario.user_id == current_user.id)
        .order_by(DetectionEvent.detected_at.desc())
    ).all()


@router.get("/{event_id}", response_model=DetectionEventResponse)
def get_event(
    event_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    event = db.get(DetectionEvent, event_id)
    if not event or event.scenario.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Event not found")
    return event
