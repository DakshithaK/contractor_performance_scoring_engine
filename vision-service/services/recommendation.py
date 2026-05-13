"""Claude recommendation text generator (haiku model)."""
from __future__ import annotations

import logging
import os

from anthropic import Anthropic

log = logging.getLogger(__name__)

SYSTEM_PROMPT = "You are a senior construction ops manager at a large Indian construction company."

USER_TEMPLATE = (
    "Generate a hire/don't-hire recommendation for this contractor based on their "
    "performance data:\n\n"
    "Contractor: {name}, {city}, Trade: {trade}\n"
    "Overall Score: {overall_score}/100\n"
    "- Delay Score: {delay_score}/100 (on-time delivery)\n"
    "- Budget Score: {budget_score}/100 (cost control)\n"
    "- Quality Score: {quality_score}/100 (site photo analysis)\n"
    "- Customer Score: {customer_score}/100 (customer ratings)\n"
    "Projects completed: {project_count}\n"
    "Recommendation tier: {recommendation}\n\n"
    "Write 3-4 sentences. Be direct and specific. Mention their strongest dimension "
    "and their biggest risk. End with a clear action: 'Recommended for high-value projects', "
    "'Use only for low-risk projects', or 'Do not engage until issues resolved'."
)


class RecommendationGenerator:
    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.environ.get("ANTHROPIC_API_KEY")
        self.model = model or os.environ.get(
            "RECOMMENDATION_MODEL", "claude-haiku-4-5-20251001"
        )
        self._client = Anthropic(api_key=self.api_key) if self.api_key else None

    def generate(self, payload: dict) -> str:
        if not self._client:
            log.warning("ANTHROPIC_API_KEY not set — returning fallback reasoning")
            return self._fallback(payload)

        prompt = USER_TEMPLATE.format(**payload)
        response = self._client.messages.create(
            model=self.model,
            max_tokens=400,
            system=SYSTEM_PROMPT,
            messages=[{"role": "user", "content": prompt}],
        )
        return "".join(block.text for block in response.content if block.type == "text").strip()

    @staticmethod
    def _fallback(p: dict) -> str:
        return (
            f"{p['name']} ({p['city']}, {p['trade']}) has an overall score of "
            f"{p['overall_score']}/100. Tier: {p['recommendation']}. "
            "Detailed reasoning unavailable (LLM offline)."
        )
