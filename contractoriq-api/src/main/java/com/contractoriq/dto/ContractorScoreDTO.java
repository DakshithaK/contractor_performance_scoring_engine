package com.contractoriq.dto;

import com.contractoriq.models.Recommendation;
import com.contractoriq.models.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ContractorScoreDTO {

    private UUID contractorId;
    private String name;
    private String city;
    private Trade trade;
    private int projectCount;
    private Instant calculatedAt;
    private BigDecimal delayScore;
    private BigDecimal budgetScore;
    private BigDecimal qualityScore;
    private BigDecimal customerScore;
    private BigDecimal overallScore;
    private Recommendation recommendation;
    private String recommendationReasoning;
    private boolean qualityUnverified;

    public ContractorScoreDTO() {}

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }

    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    public BigDecimal getDelayScore() { return delayScore; }
    public void setDelayScore(BigDecimal delayScore) { this.delayScore = delayScore; }

    public BigDecimal getBudgetScore() { return budgetScore; }
    public void setBudgetScore(BigDecimal budgetScore) { this.budgetScore = budgetScore; }

    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }

    public BigDecimal getCustomerScore() { return customerScore; }
    public void setCustomerScore(BigDecimal customerScore) { this.customerScore = customerScore; }

    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }

    public Recommendation getRecommendation() { return recommendation; }
    public void setRecommendation(Recommendation recommendation) { this.recommendation = recommendation; }

    public String getRecommendationReasoning() { return recommendationReasoning; }
    public void setRecommendationReasoning(String recommendationReasoning) {
        this.recommendationReasoning = recommendationReasoning;
    }

    public boolean isQualityUnverified() { return qualityUnverified; }
    public void setQualityUnverified(boolean qualityUnverified) { this.qualityUnverified = qualityUnverified; }
}
