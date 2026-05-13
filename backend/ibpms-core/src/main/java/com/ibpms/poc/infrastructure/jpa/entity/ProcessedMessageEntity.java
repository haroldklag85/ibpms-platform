package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "ibpms_processed_messages")
public class ProcessedMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "queue_name", length = 100, nullable = false)
    private String queueName;

    public ProcessedMessageEntity() {
        this.processedAt = LocalDateTime.now();
    }

    public ProcessedMessageEntity(UUID idempotencyKey, String queueName) {
        this.idempotencyKey = idempotencyKey;
        this.queueName = queueName;
        this.processedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(UUID idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
}
