package com.contractoriq.services;

import com.contractoriq.models.Contractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final VisionServiceClient visionServiceClient;

    public RecommendationService(VisionServiceClient visionServiceClient) {
        this.visionServiceClient = visionServiceClient;
    }

    public String generateReasoning(Contractor contractor, ScoringEngine.ScoreResult result) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", contractor.getName());
        payload.put("city", contractor.getCity());
        payload.put("trade", contractor.getTrade().name());
        payload.put("overall_score", result.overallScore);
        payload.put("delay_score", result.delayScore);
        payload.put("budget_score", result.budgetScore);
        payload.put("quality_score", result.qualityScore);
        payload.put("customer_score", result.customerScore);
        payload.put("project_count", result.projectCount);
        payload.put("recommendation", result.recommendation.name());

        try {
            return visionServiceClient.generateRecommendation(payload);
        } catch (Exception ex) {
            log.warn("vision-service recommendation call failed: {}", ex.getMessage());
            return fallback(contractor, result);
        }
    }

    private String fallback(Contractor contractor, ScoringEngine.ScoreResult r) {
        return String.format(
                "%s (%s, %s) — overall %s/100. Recommendation: %s. Reasoning unavailable (LLM offline).",
                contractor.getName(),
                contractor.getCity(),
                contractor.getTrade().name(),
                r.overallScore.toPlainString(),
                r.recommendation.name());
    }
}
