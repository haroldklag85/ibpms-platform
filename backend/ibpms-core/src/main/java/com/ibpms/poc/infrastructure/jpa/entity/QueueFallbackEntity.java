package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_queue_fallback")
public class QueueFallbackEntity {

    @Id
    private UUID id;

    @Column(name = "message_body", columnDefinition = "TEXT", nullable = false)
    private String messageBody;

    @Column(name = "target_queue", length = 100, nullable = false)
    private String targetQueue;

    @Column(name = "headers_json", columnDefinition = "TEXT")
    private String headersJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public QueueFallbackEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }
    public String getTargetQueue() { return targetQueue; }
    public void setTargetQueue(String targetQueue) { this.targetQueue = targetQueue; }
    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
