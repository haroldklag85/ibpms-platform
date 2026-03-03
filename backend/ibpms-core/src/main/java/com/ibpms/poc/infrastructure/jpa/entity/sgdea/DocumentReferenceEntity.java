package com.ibpms.poc.infrastructure.jpa.entity.sgdea;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA - Metadatos de un Documento (SGDEA CA-1, CA-4, CA-5, CA-8).
 * Relaciona el archivo almacenado en SharePoint con el entorno iBPMS.
 */
@Entity
@Table(name = "ibpms_document_reference")
public class DocumentReferenceEntity {

    @Id
    private UUID id;

    @Column(name = "sharepoint_graph_id", nullable = false, length = 150)
    private String sharepointGraphId;

    @Column(name = "sharepoint_url", length = 500)
    private String sharepointUrl;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;

    @Column(name = "trd_expiration_date")
    private LocalDate trdExpirationDate;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "process_instance_id", length = 64)
    private String processInstanceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    public DocumentReferenceEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.version = 1;
    }

    public DocumentReferenceEntity(String sharepointGraphId, String sharepointUrl, String fileName,
            String mimeType, String sha256Hash, String processInstanceId, String uploadedBy) {
        this();
        this.sharepointGraphId = sharepointGraphId;
        this.sharepointUrl = sharepointUrl;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sha256Hash = sha256Hash;
        this.processInstanceId = processInstanceId;
        this.uploadedBy = uploadedBy;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getSharepointGraphId() {
        return sharepointGraphId;
    }

    public String getSharepointUrl() {
        return sharepointUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public LocalDate getTrdExpirationDate() {
        return trdExpirationDate;
    }

    public Integer getVersion() {
        return version;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    // Setters
    public void setTrdExpirationDate(LocalDate trdExpirationDate) {
        this.trdExpirationDate = trdExpirationDate;
    }

    public void incrementVersion() {
        this.version++;
    }

    public void setSharepointGraphId(String sharepointGraphId) {
        this.sharepointGraphId = sharepointGraphId;
    }

    public void setSharepointUrl(String sharepointUrl) {
        this.sharepointUrl = sharepointUrl;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }
}
