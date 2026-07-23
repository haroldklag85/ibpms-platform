package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.ibpms.poc.crosscutting.annotations.Traceability;

@Entity
@Table(name = "ibpms_time_logs")
@Traceability(US = "US-008", CA = {"CA-09", "CA-11"})
public class TimeLogEntity {
    @Id
    private UUID id;

    @Column(name = "reference_id", nullable = false, updatable = false)
    private UUID referenceId;

    @Column(name = "reference_type", nullable = false, updatable = false)
    private String referenceType;

    @Column(name = "started_at", nullable = false, updatable = false)
    private ZonedDateTime startedAt;

    @Column(name = "stopped_at")
    private ZonedDateTime stoppedAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public ZonedDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(ZonedDateTime stoppedAt) { this.stoppedAt = stoppedAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
