package com.ibpms.poc.application.dto.security;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CA-9 US-036 — DTO de Solicitud de Cesión Temporal de Poder.
 *
 * Recibe el receptor de la delegación, el rango de fechas y el motivo auditable.
 * La validación de fechas retroactivas e invertidas se realiza en DelegationService.
 */
public class DelegationRequestDTO {

    /** UUID del usuario que recibirá los privilegios (sustituto). */
    private UUID recipientId;

    /** Inicio de la ventana de delegación — no puede ser retroactivo. */
    private LocalDateTime startDate;

    /** Fin de la ventana de delegación — debe ser posterior a startDate. */
    private LocalDateTime endDate;

    /** Motivo auditable de la cesión (opcional, máx 500 chars). */
    private String reason;

    public DelegationRequestDTO() {}

    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(UUID recipientId) { this.recipientId = recipientId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
