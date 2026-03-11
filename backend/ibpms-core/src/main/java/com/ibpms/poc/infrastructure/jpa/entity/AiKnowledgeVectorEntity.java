package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_knowledge_vectors")
public class AiKnowledgeVectorEntity {

    @Id
    private UUID id;

    @Column(name = "context_email_body", columnDefinition = "TEXT")
    private String contextEmailBody;

    @Column(name = "human_approved_reply", columnDefinition = "TEXT")
    private String humanApprovedReply;

    @Column(name = "embedding", columnDefinition = "vector(1536)") // 1536 es la dimensión de text-embedding-3-small y
                                                                   // ada-002
    private float[] embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AiKnowledgeVectorEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContextEmailBody() {
        return contextEmailBody;
    }

    public void setContextEmailBody(String contextEmailBody) {
        this.contextEmailBody = contextEmailBody;
    }

    public String getHumanApprovedReply() {
        return humanApprovedReply;
    }

    public void setHumanApprovedReply(String humanApprovedReply) {
        this.humanApprovedReply = humanApprovedReply;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
