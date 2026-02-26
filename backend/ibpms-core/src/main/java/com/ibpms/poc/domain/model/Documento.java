package com.ibpms.poc.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de Dominio: Documento.
 * Agnóstica a la nube, representa la metadata legal del SGDEA
 * independientemente
 * de si está guardada en LocalStorage, Azure BlobStorage o S3.
 */
public class Documento {

    private final UUID id;
    private final UUID caseId;
    private final String documentTypeCode;
    private final String fileName;
    private final String blobUri;
    private final String sha256Hash;
    private final String uploadedBy;
    private final Instant uploadedAt;

    private Documento(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.caseId = builder.caseId;
        this.documentTypeCode = builder.documentTypeCode;
        this.fileName = builder.fileName;
        this.blobUri = builder.blobUri;
        this.sha256Hash = builder.sha256Hash;
        this.uploadedBy = builder.uploadedBy;
        this.uploadedAt = builder.uploadedAt != null ? builder.uploadedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCaseId() {
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

    public static class Builder {
        private UUID id;
        private UUID caseId;
        private String documentTypeCode;
        private String fileName;
        private String blobUri;
        private String sha256Hash;
        private String uploadedBy;
        private Instant uploadedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder caseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder documentTypeCode(String code) {
            this.documentTypeCode = code;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder blobUri(String blobUri) {
            this.blobUri = blobUri;
            return this;
        }

        public Builder sha256Hash(String hash) {
            this.sha256Hash = hash;
            return this;
        }

        public Builder uploadedBy(String uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }

        public Builder uploadedAt(Instant uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        public Documento build() {
            // Validaciones básicas de integridad de reglas de negocio
            if (caseId == null)
                throw new IllegalArgumentException("El Documento requiere un expediente origen (caseId)");
            if (fileName == null || fileName.isBlank())
                throw new IllegalArgumentException("El Documento requiere un nombre de archivo válido");
            if (blobUri == null || blobUri.isBlank())
                throw new IllegalArgumentException("El Documento requiere una URI de almacenamiento válida");
            if (sha256Hash == null || sha256Hash.isBlank())
                throw new IllegalArgumentException(
                        "El Documento requiere una huella SHA-256 inmutable (Validación de no repudio)");
            return new Documento(this);
        }
    }
}
