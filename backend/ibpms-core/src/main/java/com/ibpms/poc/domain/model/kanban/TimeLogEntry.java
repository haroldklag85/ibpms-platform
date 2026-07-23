package com.ibpms.poc.domain.model.kanban;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TimeLogEntry {
    private UUID id;
    private final UUID referenceId;
    private String referenceType;
    private final ZonedDateTime startedAt;
    private ZonedDateTime stoppedAt;
    private Integer durationMinutes;
    private final String userId;
    private ZonedDateTime createdAt;

    public TimeLogEntry(UUID id, UUID referenceId, String referenceType, ZonedDateTime startedAt, ZonedDateTime stoppedAt, Integer durationMinutes, String userId, ZonedDateTime createdAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.startedAt = startedAt != null ? startedAt : ZonedDateTime.now();
        this.stoppedAt = stoppedAt;
        this.durationMinutes = durationMinutes;
        this.userId = userId;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    public void stop(ZonedDateTime now) {
        if (this.stoppedAt != null) {
            throw new IllegalStateException("El timer ya se encuentra detenido.");
        }
        this.stoppedAt = now;
        this.durationMinutes = (int) ChronoUnit.MINUTES.between(this.startedAt, this.stoppedAt);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReferenceId() { return referenceId; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public ZonedDateTime getStoppedAt() { return stoppedAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getUserId() { return userId; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
