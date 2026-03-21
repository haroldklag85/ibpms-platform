package com.ibpms.poc.infrastructure.jpa.entity.dmn;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_dmn_models")
public class DmnModelEntity {

    @Id
    @Column(length = 100)
    private String id;

    @Lob
    @Column(nullable = false)
    private String xmlContent;

    @Column(nullable = false, length = 20)
    private String status; // DRAFT o SEALED

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

    public DmnModelEntity() {}

    public DmnModelEntity(String id, String xmlContent, String status, String tenantId, String authorJwtHash) {
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
    
    public String getAuthorJwtHash() { return authorJwtHash; }
    public void setAuthorJwtHash(String authorJwtHash) { this.authorJwtHash = authorJwtHash; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getChatHistoryJson() { return chatHistoryJson; }
    public void setChatHistoryJson(String chatHistoryJson) { this.chatHistoryJson = chatHistoryJson; }

    @PreUpdate
    public void setLastUpdate() { this.updatedAt = LocalDateTime.now(); }
}
