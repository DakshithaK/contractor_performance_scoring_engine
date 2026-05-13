"""Claude Vision analysis for construction site photos."""
from __future__ import annotations

import base64
import json
import logging
import os
from typing import Any, Dict

from anthropic import Anthropic

log = logging.getLogger(__name__)

SYSTEM_PROMPT = "You are a construction site quality inspector with 20 years of experience."

USER_PROMPT = (
    "Analyze this construction site photo and return ONLY valid JSON, no other text:\n"
    "{\n"
    "  quality_score: number between 0-100,\n"
    "  cleanliness: number 0-100,\n"
    "  safety_compliance: number 0-100,\n"
    "  work_quality: number 0-100,\n"
    "  issues: [{ type: string, severity: 'low'|'medium'|'high', description: string }],\n"
    "  positive_observations: [string],\n"
    "  overall_summary: string\n"
    "}"
)


class VisionAnalyzer:
    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.environ.get("ANTHROPIC_API_KEY")
        self.model = model or os.environ.get("VISION_MODEL", "claude-opus-4-6")
        self._client = Anthropic(api_key=self.api_key) if self.api_key else None

    def analyze(self, image_bytes: bytes, media_type: str = "image/jpeg") -> Dict[str, Any]:
        if not self._client:
            log.warning("ANTHROPIC_API_KEY not set — returning placeholder analysis")
            return self._placeholder()

        b64 = base64.standard_b64encode(image_bytes).decode("utf-8")
        response = self._client.messages.create(
            model=self.model,
            max_tokens=1024,
            system=SYSTEM_PROMPT,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": media_type,
                                "data": b64,
                            },
                        },
                        {"type": "text", "text": USER_PROMPT},
                    ],
                }
            ],
        )
        raw_text = "".join(block.text for block in response.content if block.type == "text")
        parsed = self._parse_json(raw_text)
        parsed["raw"] = {"model": self.model, "response": raw_text}
        return parsed

    @staticmethod
    def _parse_json(text: str) -> Dict[str, Any]:
        try:
            return json.loads(text)
        except json.JSONDecodeError:
            start = text.find("{")
            end = text.rfind("}")
            if start != -1 and end != -1:
                try:
                    return json.loads(text[start : end + 1])
                except json.JSONDecodeError:
                    pass
            log.error("could not parse Claude vision response as JSON: %s", text[:300])
            return {
                "quality_score": 50,
                "cleanliness": 50,
                "safety_compliance": 50,
                "work_quality": 50,
                "issues": [],
                "positive_observations": [],
                "overall_summary": "Failed to parse model output.",
            }

    @staticmethod
    def _placeholder() -> Dict[str, Any]:
        return {
            "quality_score": 70,
            "cleanliness": 70,
            "safety_compliance": 70,
            "work_quality": 70,
            "issues": [],
            "positive_observations": ["Placeholder analysis — Anthropic key not configured."],
            "overall_summary": "Placeholder analysis.",
            "raw": {"placeholder": True},
        }
