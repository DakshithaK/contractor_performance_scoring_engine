"""ContractorIQ vision-service — FastAPI wrapper around Claude Vision + recommendation."""
from __future__ import annotations

import logging

from fastapi import FastAPI, File, HTTPException, UploadFile

from models import RecommendationRequest, RecommendationResponse, VisionAnalysisResponse
from services.recommendation import RecommendationGenerator
from services.vision_analyzer import VisionAnalyzer

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("vision-service")

app = FastAPI(title="ContractorIQ Vision Service", version="0.1.0")

vision = VisionAnalyzer()
recommender = RecommendationGenerator()


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/analyze-photo", response_model=VisionAnalysisResponse)
async def analyze_photo(file: UploadFile = File(...)) -> VisionAnalysisResponse:
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="file must be an image")
    image_bytes = await file.read()
    try:
        result = vision.analyze(image_bytes, media_type=file.content_type)
    except Exception as ex:
        log.exception("vision analysis failed")
        raise HTTPException(status_code=502, detail=f"vision analysis failed: {ex}")
    return VisionAnalysisResponse(**result)


@app.post("/generate-recommendation", response_model=RecommendationResponse)
def generate_recommendation(req: RecommendationRequest) -> RecommendationResponse:
    try:
        reasoning = recommender.generate(req.model_dump())
    except Exception as ex:
        log.exception("recommendation generation failed")
        raise HTTPException(status_code=502, detail=f"recommendation failed: {ex}")
    return RecommendationResponse(reasoning=reasoning)
