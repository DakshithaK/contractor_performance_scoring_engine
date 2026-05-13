package com.contractoriq.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "site_photo_analyses")
public class SitePhotoAnalysis {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column(name = "photo_path", nullable = false)
    private String photoPath;

    @Type(JsonBinaryType.class)
    @Column(name = "claude_raw_response", columnDefinition = "jsonb")
    private Map<String, Object> claudeRawResponse;

    @Column(name = "quality_score", nullable = false)
    private int qualityScore;

    @Type(JsonBinaryType.class)
    @Column(name = "issues_found", columnDefinition = "jsonb")
    private List<Map<String, Object>> issuesFound;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    public SitePhotoAnalysis() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getContractorId() { return contractorId; }
    public void setContractorId(UUID contractorId) { this.contractorId = contractorId; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public Map<String, Object> getClaudeRawResponse() { return claudeRawResponse; }
    public void setClaudeRawResponse(Map<String, Object> claudeRawResponse) {
        this.claudeRawResponse = claudeRawResponse;
    }

    public int getQualityScore() { return qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }

    public List<Map<String, Object>> getIssuesFound() { return issuesFound; }
    public void setIssuesFound(List<Map<String, Object>> issuesFound) {
        this.issuesFound = issuesFound;
    }

    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
}
