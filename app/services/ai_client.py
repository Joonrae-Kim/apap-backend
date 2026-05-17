from app.core.config import settings
from app.models.analysis_job import AnalysisJob
from app.models.scenario import Scenario
from app.models.video_source import VideoSource


def build_analysis_payload(
    job: AnalysisJob,
    scenario: Scenario,
    video_source: VideoSource,
) -> dict:
    return {
        "job_id": job.id,
        "scenario": {
            "id": scenario.id,
            "name": scenario.name,
            "behavior_text": scenario.behavior_text,
            "conditions": scenario.conditions_json,
            "threshold": scenario.threshold,
        },
        "video": {
            "id": video_source.id,
            "type": video_source.type,
            "source_url": video_source.source_url,
        },
        "callback_url": "/api/analysis/callback",
        "ai_server_url": settings.ai_server_url,
    }
