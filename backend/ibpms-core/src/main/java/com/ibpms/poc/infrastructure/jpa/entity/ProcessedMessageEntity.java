package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_processed_messages")
public class ProcessedMessageEntity {

    @Id
    @Column(name = "idempotency_key", length = 36)
    private String idempotencyKey;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "queue_name", length = 100, nullable = false)
    private String queueName;

    public ProcessedMessageEntity() {
        this.processedAt = LocalDateTime.now();
    }

    public ProcessedMessageEntity(String idempotencyKey, String queueName) {
        this.idempotencyKey = idempotencyKey;
        this.queueName = queueName;
        this.processedAt = LocalDateTime.now();
    }

    // Getters
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
}
