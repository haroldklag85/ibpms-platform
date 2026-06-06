package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_audit_reports")
public class AuditReportEntity {

    @Id
    private UUID id;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "generated_by_user_id", nullable = false, length = 100)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String fileHash;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "file_path_or_blob", columnDefinition = "jsonb")
    private String metadataJson;

    public AuditReportEntity() {
        this.id = UUID.randomUUID();
        this.generatedAt = LocalDateTime.now();
    }

    public AuditReportEntity(String reportType, String generatedBy, String fileHash, String metadataJson) {
        this();
        this.reportType = reportType;
        this.generatedBy = generatedBy;
        this.fileHash = fileHash;
        this.metadataJson = metadataJson;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
