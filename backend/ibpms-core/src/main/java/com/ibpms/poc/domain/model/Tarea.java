package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio Core: Tarea Humana.
 * Objeto Inmutable. Representa una unidad de trabajo asignada a un usuario o grupo.
 */
public class Tarea {

    private final UUID id;
    private final String name;
    private final UUID expedienteId;
    private final String assignee; // UUID del usuario
    private final List<String> candidateGroups; // Referencias a roles/colas ABAC
    private final int priority;
    private final LocalDateTime dueDate;
    private final TareaStatus status;
    private final LocalDateTime createdAt;

    public enum TareaStatus {
        PENDING, CLAIMED, COMPLETED, CANCELLED
    }

    private Tarea(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = Objects.requireNonNull(builder.name, "El nombre de la tarea es requerido");
        this.expedienteId = Objects.requireNonNull(builder.expedienteId, "expedienteId es obligatorio");
        this.assignee = builder.assignee;
        this.candidateGroups = builder.candidateGroups != null ? List.copyOf(builder.candidateGroups) : Collections.emptyList();
        this.priority = builder.priority != null ? builder.priority : 50;
        this.dueDate = builder.dueDate;
        this.status = builder.status != null ? builder.status : TareaStatus.PENDING;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    // Regla de negocio: Reclamar Tarea (Asignar a Pila de Usuario)
    public Tarea reclamar(String userId) {
        if (this.status != TareaStatus.PENDING) {
            throw new IllegalStateException("Solo las tareas PENDING pueden ser reclamadas.");
        }
        return new Builder()
                .from(this)
                .assignee(userId)
                .status(TareaStatus.CLAIMED)
                .build();
    }

    // Regla de negocio: Completar Tarea
    public Tarea completar() {
        if (this.status == TareaStatus.COMPLETED) {
            throw new IllegalStateException("La tarea ya se encuentra completada.");
        }
        return new Builder()
                .from(this)
                .status(TareaStatus.COMPLETED)
                .build();
    }

    // Getters inmutables
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getExpedienteId() { return expedienteId; }
    public String getAssignee() { return assignee; }
    public List<String> getCandidateGroups() { return candidateGroups; }
    public int getPriority() { return priority; }
    public LocalDateTime getDueDate() { return dueDate; }
    public TareaStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID id;
        private String name;
        private UUID expedienteId;
        private String assignee;
        private List<String> candidateGroups;
        private Integer priority;
        private LocalDateTime dueDate;
        private TareaStatus status;
        private LocalDateTime createdAt;

        public Builder from(Tarea existing) {
            this.id = existing.id;
            this.name = existing.name;
            this.expedienteId = existing.expedienteId;
            this.assignee = existing.assignee;
            this.candidateGroups = existing.candidateGroups;
            this.priority = existing.priority;
            this.dueDate = existing.dueDate;
            this.status = existing.status;
            this.createdAt = existing.createdAt;
            return this;
        }

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder expedienteId(UUID expedienteId) { this.expedienteId = expedienteId; return this; }
        public Builder assignee(String assignee) { this.assignee = assignee; return this; }
        public Builder candidateGroups(List<String> groups) { this.candidateGroups = groups; return this; }
        public Builder priority(Integer priority) { this.priority = priority; return this; }
        public Builder dueDate(LocalDateTime dueDate) { this.dueDate = dueDate; return this; }
        public Builder status(TareaStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Tarea build() { return new Tarea(this); }
    }
}
