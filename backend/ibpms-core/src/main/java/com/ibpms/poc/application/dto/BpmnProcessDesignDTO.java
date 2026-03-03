package com.ibpms.poc.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BpmnProcessDesignDTO {
    private UUID id;
    private String name;
    private String technicalId;
    private String formPattern;
    private String status;
    private int currentVersion;
    private String lockedBy;
    private LocalDateTime lockedAt;
    private String xmlDraft;
    private int maxNodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

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

    public String getFormPattern() {
        return formPattern;
    }

    public void setFormPattern(String formPattern) {
        this.formPattern = formPattern;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
