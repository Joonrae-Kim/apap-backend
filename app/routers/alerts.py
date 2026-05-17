from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.deps import get_current_user
from app.models.alert import Alert
from app.models.user import User
from app.schemas.alert import AlertResponse

router = APIRouter()


@router.get("", response_model=list[AlertResponse])
def list_alerts(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return db.scalars(
        select(Alert)
        .where(Alert.receiver_id == current_user.id)
        .order_by(Alert.id.desc())
    ).all()


@router.patch("/{alert_id}/read", response_model=AlertResponse)
def mark_alert_read(
    alert_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    alert = db.get(Alert, alert_id)
    if not alert or alert.receiver_id != current_user.id:
        raise HTTPException(status_code=404, detail="Alert not found")

    alert.status = "READ"
    alert.read_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(alert)
    return alert
