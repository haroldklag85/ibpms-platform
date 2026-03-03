package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agregado de Dominio — Diseño de Proceso BPMN.
 * Contiene la lógica de negocio de Lock pesimista, versionamiento y
 * transiciones de estado.
 */
public class BpmnProcessDesign {

    public enum Status {
        DRAFT, ACTIVE, PENDING_DEPLOY, ARCHIVED
    }

    public enum FormPattern {
        SIMPLE, IFORM_MAESTRO
    }

    private static final int LOCK_TTL_MINUTES = 30;

    private UUID id;
    private String name;
    private String technicalId;
    private FormPattern formPattern;
    private Status status;
    private int currentVersion;
    private String lockedBy;
    private LocalDateTime lockedAt;
    private String xmlDraft;
    private int maxNodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // Factory para creación nueva
    public static BpmnProcessDesign crear(String name, FormPattern formPattern, String createdBy) {
        BpmnProcessDesign design = new BpmnProcessDesign();
        design.id = UUID.randomUUID();
        design.name = name;
        design.technicalId = generateSlug(name);
        design.formPattern = formPattern;
        design.status = Status.DRAFT;
        design.currentVersion = 0;
        design.maxNodes = 100;
        design.createdAt = LocalDateTime.now();
        design.updatedAt = LocalDateTime.now();
        design.createdBy = createdBy;
        return design;
    }

    // Factory para reconstitución desde BD
    public static BpmnProcessDesign reconstituir(UUID id, String name, String technicalId,
            FormPattern formPattern, Status status, int currentVersion,
            String lockedBy, LocalDateTime lockedAt, String xmlDraft, int maxNodes,
            LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        BpmnProcessDesign d = new BpmnProcessDesign();
        d.id = id;
        d.name = name;
        d.technicalId = technicalId;
        d.formPattern = formPattern;
        d.status = status;
        d.currentVersion = currentVersion;
        d.lockedBy = lockedBy;
        d.lockedAt = lockedAt;
        d.xmlDraft = xmlDraft;
        d.maxNodes = maxNodes;
        d.createdAt = createdAt;
        d.updatedAt = updatedAt;
        d.createdBy = createdBy;
        return d;
    }

    // --- Lógica de Negocio: Lock Pesimista ---

    public void acquireLock(String userId) {
        if (lockedBy != null && !lockedBy.equals(userId) && !isLockExpired()) {
            throw new IllegalStateException("El proceso está bloqueado por " + lockedBy);
        }
        this.lockedBy = userId;
        this.lockedAt = LocalDateTime.now();
    }

    public void releaseLock(String userId) {
        if (lockedBy != null && !lockedBy.equals(userId)) {
            throw new IllegalStateException("Solo " + lockedBy + " puede liberar el bloqueo.");
        }
        this.lockedBy = null;
        this.lockedAt = null;
    }

    public boolean isLockExpired() {
        if (lockedAt == null)
            return true;
        return lockedAt.plusMinutes(LOCK_TTL_MINUTES).isBefore(LocalDateTime.now());
    }

    // --- Lógica de Negocio: Transiciones de Estado ---

    public void requestDeploy() {
        if (status != Status.DRAFT && status != Status.ACTIVE) {
            throw new IllegalStateException("Solo procesos en DRAFT o ACTIVE pueden solicitar despliegue.");
        }
        this.status = Status.PENDING_DEPLOY;
        this.updatedAt = LocalDateTime.now();
    }

    public void deploy() {
        if (status != Status.PENDING_DEPLOY) {
            throw new IllegalStateException("Solo procesos en PENDING_DEPLOY pueden ser desplegados.");
        }
        this.currentVersion++;
        this.status = Status.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = Status.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void resetPreFlightOnEdit() {
        // CA-24: El estado del pre-flight se resetea tras cualquier edición del XML
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDraft(String xml) {
        this.xmlDraft = xml;
        this.updatedAt = LocalDateTime.now();
    }

    // --- Slug Generator (CA-17) ---
    private static String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // --- Getters ---
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTechnicalId() {
        return technicalId;
    }

    public FormPattern getFormPattern() {
        return formPattern;
    }

    public Status getStatus() {
        return status;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public String getXmlDraft() {
        return xmlDraft;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
