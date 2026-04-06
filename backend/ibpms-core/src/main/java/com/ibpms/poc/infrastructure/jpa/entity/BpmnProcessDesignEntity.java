package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_bpmn_process_design")
public class BpmnProcessDesignEntity {

    public enum Status {
        DRAFT, ACTIVE, PENDING_DEPLOY, ARCHIVED
    }

    public enum FormPattern {
        SIMPLE, IFORM_MAESTRO
    }

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "technical_id", nullable = false, unique = true, length = 200)
    private String technicalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_pattern", nullable = false, length = 30)
    private FormPattern formPattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "xml_draft", columnDefinition = "TEXT")
    private String xmlDraft;

    @Column(name = "generic_form_whitelist", columnDefinition = "jsonb")
    private String genericFormWhitelist;

    @Column(name = "max_nodes", nullable = false)
    private int maxNodes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Version
    @Column(name = "opt_lock_version")
    private Long optLockVersion;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTechnicalId() {
        return technicalId;
    }

    public void setTechnicalId(String technicalId) {
        this.technicalId = technicalId;
    }

    public FormPattern getFormPattern() {
        return formPattern;
    }

    public void setFormPattern(FormPattern formPattern) {
        this.formPattern = formPattern;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getXmlDraft() {
        return xmlDraft;
    }

    public void setXmlDraft(String xmlDraft) {
        this.xmlDraft = xmlDraft;
    }

    public String getGenericFormWhitelist() {
        return genericFormWhitelist;
    }

    public void setGenericFormWhitelist(String genericFormWhitelist) {
        this.genericFormWhitelist = genericFormWhitelist;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
