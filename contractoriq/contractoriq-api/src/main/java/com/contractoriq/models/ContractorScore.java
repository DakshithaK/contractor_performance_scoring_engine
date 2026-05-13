package com.contractoriq.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contractor_scores")
public class ContractorScore implements Serializable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "delay_score", nullable = false, precision = 6, scale = 2)
    private BigDecimal delayScore;

    @Column(name = "budget_score", nullable = false, precision = 6, scale = 2)
    private BigDecimal budgetScore;

    @Column(name = "quality_score", nullable = false, precision = 6, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "customer_score", nullable = false, precision = 6, scale = 2)
    private BigDecimal customerScore;

    @Column(name = "overall_score", nullable = false, precision = 6, scale = 2)
    private BigDecimal overallScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recommendation recommendation;

    @Column(name = "recommendation_reasoning", columnDefinition = "text")
    private String recommendationReasoning;

    public ContractorScore() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

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
}
