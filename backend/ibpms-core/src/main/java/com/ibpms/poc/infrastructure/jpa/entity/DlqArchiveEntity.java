package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_dlq_archive")
public class DlqArchiveEntity {

    @Id
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "original_queue", length = 100)
    private String originalQueue;

    @Column(name = "headers_json", columnDefinition = "TEXT")
    private String headersJson;

    @Column(name = "body_summary", length = 1024)
    private String bodySummary;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    public DlqArchiveEntity() {
        this.archivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getOriginalQueue() { return originalQueue; }
    public void setOriginalQueue(String originalQueue) { this.originalQueue = originalQueue; }
    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }
    public String getBodySummary() { return bodySummary; }
    public void setBodySummary(String bodySummary) { this.bodySummary = bodySummary; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
}
