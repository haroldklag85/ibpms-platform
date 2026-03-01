package com.ibpms.poc.application.dto;

import java.time.LocalDateTime;

/**
 * DTO para la consulta pública de trazabilidad de un trámite (Portal B2C/B2B).
 * Implementa el patrón Anti-Corrupción evitando exponer IDs del motor de
 * procesos.
 */
public class PublicTrackingDTO {

    private String trackingCode;
    private String statusDescription; // Ej: "En revisión legal" (Amigable)
    private LocalDateTime startedAt;
    private boolean isCompleted;

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
