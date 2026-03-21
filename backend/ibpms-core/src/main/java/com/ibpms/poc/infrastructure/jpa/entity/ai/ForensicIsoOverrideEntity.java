package com.ibpms.poc.infrastructure.jpa.entity.ai;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CA-09: Entidad Forense Inmutable para Audit-Trail ISO 9001.
 * Representa una "Firma Culpable" del modelador al descartar advertencias topológicas.
 */
@Entity
@Table(name = "ibpms_forensic_iso_overrides", 
       indexes = {
           @Index(name = "idx_forensic_user", columnList = "user_id"),
           @Index(name = "idx_forensic_session", columnList = "session_id")
       })
public class ForensicIsoOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId; // Extraído limpiamente desde JWT

    @Column(name = "session_id", nullable = false, length = 150)
    private String sessionId;

    @Column(name = "ignored_warning_code", nullable = false, length = 100)
    private String ignoredWarningCode;

    // XML Forzado que transgrede las normas
    @Lob
    @Column(name = "forced_xml", nullable = false)
    private String forcedXml;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "override_timestamp", nullable = false, updatable = false)
    private LocalDateTime overrideTimestamp;

    public ForensicIsoOverrideEntity() {
    }

    public ForensicIsoOverrideEntity(String userId, String sessionId, String ignoredWarningCode, String forcedXml, String metricsJson) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.ignoredWarningCode = ignoredWarningCode;
        this.forcedXml = forcedXml;
        this.metricsJson = metricsJson;
    }

    @PrePersist
    public void sealTimestamp() {
        this.overrideTimestamp = LocalDateTime.now();
    }

    // Getters estrictos (Sin setters que no sean necesarios garantizando Ledger Mode)
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public String getIgnoredWarningCode() { return ignoredWarningCode; }
    public String getForcedXml() { return forcedXml; }
    public String getMetricsJson() { return metricsJson; }
    public LocalDateTime getOverrideTimestamp() { return overrideTimestamp; }
}
