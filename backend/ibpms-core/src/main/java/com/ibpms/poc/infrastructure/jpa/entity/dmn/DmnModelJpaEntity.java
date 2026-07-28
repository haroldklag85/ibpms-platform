// @Traceability: US-007, US-005, CA-41 - ADR-001
package com.ibpms.poc.infrastructure.jpa.entity.dmn;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Entidad JPA para persistencia de modelos DMN.
 * @Traceability: US-007 - Generador Cognitivo de DMN (NLP a Tablas de Decisión)
 */
@Entity
@Table(name = "ibpms_dmn_models")
@Traceability(US = "US-007", CA = {"CA-05", "CA-06", "CA-12", "CA-32"})
public class DmnModelJpaEntity {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "xml_content", columnDefinition = "TEXT", nullable = false)
    private String xmlContent;

    @Column(nullable = false, length = 20)
    private String status; // DRAFT o SEALED

    @Column(length = 200)
    private String name;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // CA-05: Trazabilidad irrepudiable (Anti-Spoofing) extraída vía JWT
    @Column(name = "author_jwt_hash", length = 255)
    private String authorJwtHash;

    // CA-06: Segregación corporativa BOLA/IDOR
    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    // CA-12: Trazabilidad del Chat NLP (Conversación Humano-IA). Persistencia Forense.
    @Column(name = "chat_history_json", columnDefinition = "TEXT")
    private String chatHistoryJson;

    // US-007 CA-32: Trazabilidad de modificación manual
    @Column(name = "is_manual")
    private Boolean isManual;

    public DmnModelJpaEntity() {}

    public DmnModelJpaEntity(String id, String xmlContent, String status, String tenantId, String authorJwtHash) {
        this.id = id;
        this.xmlContent = xmlContent;
        this.status = status;
        this.tenantId = tenantId;
        this.authorJwtHash = authorJwtHash;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getAuthorJwtHash() { return authorJwtHash; }
    public void setAuthorJwtHash(String authorJwtHash) { this.authorJwtHash = authorJwtHash; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getChatHistoryJson() { return chatHistoryJson; }
    public void setChatHistoryJson(String chatHistoryJson) { this.chatHistoryJson = chatHistoryJson; }

    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }

    @PreUpdate
    public void setLastUpdate() { this.updatedAt = LocalDateTime.now(); }
}
