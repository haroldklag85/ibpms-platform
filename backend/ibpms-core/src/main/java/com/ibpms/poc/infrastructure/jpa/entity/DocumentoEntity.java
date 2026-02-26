package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA: ibpms_document
 * Representa fidedignamente la tabla de base de datos relacional para el SGDEA,
 * de acuerdo con la Arquitectura de Datos (ERD).
 */
@Entity
@Table(name = "ibpms_document")
public class DocumentoEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "case_id", columnDefinition = "CHAR(36)", nullable = false)
    private String caseId;

    @Column(name = "document_type_code", length = 100, nullable = false)
    private String documentTypeCode;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "blob_uri", length = 500, nullable = false)
    private String blobUri;

    @Column(name = "sha256_hash", length = 64, nullable = false)
    private String sha256Hash;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBlobUri() {
        return blobUri;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBlobUri(String blobUri) {
        this.blobUri = blobUri;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
