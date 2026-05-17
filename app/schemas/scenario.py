from pydantic import BaseModel


class ScenarioBase(BaseModel):
    name: str
    description: str | None = None
    target_location: str | None = None
    behavior_text: str
    conditions_json: dict | None = None
    threshold: float = 0.8
    is_active: bool = True


class ScenarioCreate(ScenarioBase):
    pass


class ScenarioUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    target_location: str | None = None
    behavior_text: str | None = None
    conditions_json: dict | None = None
    threshold: float | None = None
    is_active: bool | None = None


class ScenarioResponse(ScenarioBase):
    id: int
    user_id: int

    model_config = {"from_attributes": True}
