from pathlib import Path
from uuid import uuid4

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.deps import get_current_user
from app.models.user import User
from app.models.video_source import VideoSource
from app.schemas.video import VideoSourceCreate, VideoSourceResponse, VideoSourceUpdate

router = APIRouter()
UPLOAD_DIR = Path("uploads")


@router.post("", response_model=VideoSourceResponse)
def create_video_source(
    payload: VideoSourceCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    video_source = VideoSource(user_id=current_user.id, **payload.model_dump())
    db.add(video_source)
    db.commit()
    db.refresh(video_source)
    return video_source


@router.post("/upload", response_model=VideoSourceResponse)
def upload_video(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    UPLOAD_DIR.mkdir(exist_ok=True)
    suffix = Path(file.filename or "").suffix
    stored_name = f"{uuid4().hex}{suffix}"
    stored_path = UPLOAD_DIR / stored_name

    with stored_path.open("wb") as buffer:
        while chunk := file.file.read(1024 * 1024):
            buffer.write(chunk)

    video_source = VideoSource(
        user_id=current_user.id,
        type="UPLOAD",
        name=file.filename or stored_name,
        source_url=str(stored_path.as_posix()),
        status="READY",
    )
    db.add(video_source)
    db.commit()
    db.refresh(video_source)
    return video_source


@router.get("", response_model=list[VideoSourceResponse])
def list_video_sources(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return db.scalars(
        select(VideoSource)
        .where(VideoSource.user_id == current_user.id)
        .order_by(VideoSource.id.desc())
    ).all()


@router.patch("/{video_id}", response_model=VideoSourceResponse)
def update_video_source(
    video_id: int,
    payload: VideoSourceUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    video_source = db.get(VideoSource, video_id)
    if not video_source or video_source.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Video source not found")

    for key, value in payload.model_dump(exclude_unset=True).items():
        setattr(video_source, key, value)

    db.commit()
    db.refresh(video_source)
    return video_source
