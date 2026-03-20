package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CA-11: Entidad Forense Inmutable para Audit-Logs Críticos.
 * Tablero de Anomalías (Ej: SoD Auto-Aprobación, Break-Glass Root Login).
 */
@Entity
@Table(name = "ibpms_security_anomalies")
public class SecurityAnomalyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String type; // INTENTO_SOD_AUTOAPROBACION, BREAK_GLASS_ACTIVATION

    @Column(name = "suspect_user_id", nullable = true, length = 100)
    private String suspectUserId;

    @Column(name = "resource_id", nullable = true, length = 100)
    private String resourceId; // Puede ser un taskId, etc.

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    private String status; // OPEN, RESOLVED

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public SecurityAnomalyEntity() {
    }

    public SecurityAnomalyEntity(String type, String suspectUserId, String resourceId, LocalDateTime timestamp, String status) {
        this.type = type;
        this.suspectUserId = suspectUserId;
        this.resourceId = resourceId;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSuspectUserId() { return suspectUserId; }
    public void setSuspectUserId(String suspectUserId) { this.suspectUserId = suspectUserId; }
    
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
