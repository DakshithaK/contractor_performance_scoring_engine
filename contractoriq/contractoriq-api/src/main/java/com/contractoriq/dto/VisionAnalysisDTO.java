package com.contractoriq.dto;

import java.util.List;
import java.util.Map;

public class VisionAnalysisDTO {

    private int qualityScore;
    private int cleanliness;
    private int safetyCompliance;
    private int workQuality;
    private List<Map<String, Object>> issues;
    private List<String> positiveObservations;
    private String overallSummary;
    private Map<String, Object> raw;

    public VisionAnalysisDTO() {}

    public int getQualityScore() { return qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }

    public int getCleanliness() { return cleanliness; }
    public void setCleanliness(int cleanliness) { this.cleanliness = cleanliness; }

    public int getSafetyCompliance() { return safetyCompliance; }
    public void setSafetyCompliance(int safetyCompliance) { this.safetyCompliance = safetyCompliance; }

    public int getWorkQuality() { return workQuality; }
    public void setWorkQuality(int workQuality) { this.workQuality = workQuality; }

    public List<Map<String, Object>> getIssues() { return issues; }
    public void setIssues(List<Map<String, Object>> issues) { this.issues = issues; }

    public List<String> getPositiveObservations() { return positiveObservations; }
    public void setPositiveObservations(List<String> positiveObservations) {
        this.positiveObservations = positiveObservations;
    }

    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public Map<String, Object> getRaw() { return raw; }
    public void setRaw(Map<String, Object> raw) { this.raw = raw; }
}
