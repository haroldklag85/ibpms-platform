package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * DTO de transferencia para Expediente.
 * Usado tanto en la petición (POST) como en la respuesta (201 Created).
 */
public class ExpedienteDTO {

    // --- Campos de respuesta ---
    private String id;
    private String status;
    private String createdAt;

    // --- Campos de petición (requeridos) ---
    @NotBlank(message = "definitionKey es requerido")
    private String definitionKey;

    @NotBlank(message = "businessKey es requerido")
    private String businessKey;

    private String type;

    @NotNull(message = "variables es requerido")
    private Map<String, Object> variables;

    // Cabecera de idempotencia (opcional, manejado por el servicio)
    private String idempotencyKey;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public void setDefinitionKey(String v) {
        this.definitionKey = v;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String v) {
        this.businessKey = v;
    }

    public String getType() {
        return type;
    }

    public void setType(String v) {
        this.type = v;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> v) {
        this.variables = v;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String v) {
        this.idempotencyKey = v;
    }
}
