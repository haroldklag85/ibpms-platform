package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_audit_log")
public class AiAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, length = 50)
    private String businessKey;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "operator_id", length = 100)
    private String operatorId;

    @Column(name = "ai_model_version", length = 50)
    private String aiModelVersion;

    @Column(name = "applied_prompt", columnDefinition = "TEXT")
    private String appliedPrompt;

    @Column(name = "content_payload", nullable = false, columnDefinition = "TEXT")
    private String contentPayload;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AiAuditLogEntity() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters...
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getAiModelVersion() {
        return aiModelVersion;
    }

    public void setAiModelVersion(String aiModelVersion) {
        this.aiModelVersion = aiModelVersion;
    }

    public String getAppliedPrompt() {
        return appliedPrompt;
    }

    public void setAppliedPrompt(String appliedPrompt) {
        this.appliedPrompt = appliedPrompt;
    }

    public String getContentPayload() {
        return contentPayload;
    }

    public void setContentPayload(String contentPayload) {
        this.contentPayload = contentPayload;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
