from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class VisionIssue(BaseModel):
    type: str
    severity: str
    description: str


class VisionAnalysisResponse(BaseModel):
    quality_score: int = Field(ge=0, le=100)
    cleanliness: int = Field(ge=0, le=100)
    safety_compliance: int = Field(ge=0, le=100)
    work_quality: int = Field(ge=0, le=100)
    issues: List[Dict[str, Any]] = []
    positive_observations: List[str] = []
    overall_summary: str = ""
    raw: Optional[Dict[str, Any]] = None


class RecommendationRequest(BaseModel):
    name: str
    city: str
    trade: str
    overall_score: float
    delay_score: float
    budget_score: float
    quality_score: float
    customer_score: float
    project_count: int
    recommendation: str


class RecommendationResponse(BaseModel):
    reasoning: str
