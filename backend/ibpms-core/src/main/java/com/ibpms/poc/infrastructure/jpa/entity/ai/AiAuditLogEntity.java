package com.ibpms.poc.infrastructure.jpa.entity.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable ledger for AI interactions (RLHF, Chain of Thought, Overrides)
 */
@Entity
@Table(name = "ibpms_ai_audit_log")
public class AiAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "execution_id", nullable = false, length = 64)
    private String executionId;

    @Column(name = "system_prompt", columnDefinition = "TEXT", nullable = false)
    private String systemPrompt;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "chain_of_thought", columnDefinition = "TEXT")
    private String chainOfThought;

    @Column(name = "human_override", columnDefinition = "TEXT")
    private String humanOverride;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AiAuditLogEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public String getChainOfThought() {
        return chainOfThought;
    }

    public String getHumanOverride() {
        return humanOverride;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public void setChainOfThought(String chainOfThought) {
        this.chainOfThought = chainOfThought;
    }

    public void setHumanOverride(String humanOverride) {
        this.humanOverride = humanOverride;
    }
}
