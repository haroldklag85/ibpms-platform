package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio Core: Expediente / Caso.
 * Objeto Inmutable — Hexagonal Architecture + DDD.
 * Sin dependencias a frameworks externos (Spring, JPA, Camunda).
 */
public class Expediente {

    private final UUID id;
    private final String definitionKey; // Ej: "reclamo-cliente"
    private final String businessKey; // Ej: "REQ-2026-001"
    private final String type; // Tipo de expediente (ERD: ibpms_case.type)
    private final ExpedienteStatus status;
    private final Map<String, Object> variables; // Estado/JSON del formulario
    private final String processInstanceId; // Ancora Camunda 7 (ADR-003)
    private final LocalDateTime createdAt;

    public enum ExpedienteStatus {
        ACTIVE, COMPLETED, SUSPENDED, CANCELLED
    }

    private Expediente(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.definitionKey = Objects.requireNonNull(builder.definitionKey, "definitionKey es requerido");
        this.businessKey = builder.businessKey;
        this.type = builder.type;
        this.status = builder.status != null ? builder.status : ExpedienteStatus.ACTIVE;
        this.variables = builder.variables != null ? new HashMap<>(builder.variables) : new HashMap<>();
        this.processInstanceId = builder.processInstanceId;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    // ── Reglas de Negocio ──────────────────────────────────────────────────────

    /** Factory: crea un expediente nuevo con estado ACTIVE. */
    public static Expediente iniciarNuevo(String definitionKey, String businessKey,
            String type, Map<String, Object> initialVariables) {
        return new Builder()
                .definitionKey(definitionKey)
                .businessKey(businessKey)
                .type(type)
                .variables(initialVariables)
                .status(ExpedienteStatus.ACTIVE)
                .build();
    }

    /** Asocia el processInstanceId de Camunda (se llama tras startProcess). */
    public Expediente vincularProceso(String processInstanceId) {
        return new Builder()
                .id(this.id)
                .definitionKey(this.definitionKey)
                .businessKey(this.businessKey)
                .type(this.type)
                .status(this.status)
                .variables(this.variables)
                .createdAt(this.createdAt)
                .processInstanceId(processInstanceId)
                .build();
    }

    /** Actualiza variables (inmutable: devuelve nuevo objeto). */
    public Expediente actualizarVariables(Map<String, Object> nuevasVariables) {
        Map<String, Object> merged = new HashMap<>(this.variables);
        merged.putAll(nuevasVariables);
        return new Builder()
                .id(this.id).definitionKey(this.definitionKey).businessKey(this.businessKey)
                .type(this.type).status(this.status).processInstanceId(this.processInstanceId)
                .createdAt(this.createdAt).variables(merged).build();
    }

    /** Completa/cierra el expediente. */
    public Expediente completar() {
        if (this.status == ExpedienteStatus.COMPLETED) {
            throw new IllegalStateException("El expediente ya está completado.");
        }
        return new Builder()
                .id(this.id).definitionKey(this.definitionKey).businessKey(this.businessKey)
                .type(this.type).processInstanceId(this.processInstanceId)
                .createdAt(this.createdAt).variables(this.variables)
                .status(ExpedienteStatus.COMPLETED).build();
    }

    // ── Getters Inmutables ─────────────────────────────────────────────────────
    public UUID getId() {
        return id;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getType() {
        return type;
    }

    public ExpedienteStatus getStatus() {
        return status;
    }

    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ── Builder ────────────────────────────────────────────────────────────────
    public static class Builder {
        private UUID id;
        private String definitionKey;
        private String businessKey;
        private String type;
        private ExpedienteStatus status;
        private Map<String, Object> variables;
        private String processInstanceId;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder definitionKey(String v) {
            this.definitionKey = v;
            return this;
        }

        public Builder businessKey(String v) {
            this.businessKey = v;
            return this;
        }

        public Builder type(String v) {
            this.type = v;
            return this;
        }

        public Builder status(ExpedienteStatus v) {
            this.status = v;
            return this;
        }

        public Builder variables(Map<String, Object> v) {
            this.variables = v;
            return this;
        }

        public Builder processInstanceId(String v) {
            this.processInstanceId = v;
            return this;
        }

        public Builder createdAt(LocalDateTime v) {
            this.createdAt = v;
            return this;
        }

        public Expediente build() {
            return new Expediente(this);
        }
    }
}
