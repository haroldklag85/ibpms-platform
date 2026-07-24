// @Traceability: US-007 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;

public class DmnModel {

    private String id;
    private String xmlContent;
    private String status;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorJwtHash;
    private String tenantId;
    private String chatHistoryJson;
    private Boolean isManual;

    public DmnModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAuthorJwtHash() { return authorJwtHash; }
    public void setAuthorJwtHash(String authorJwtHash) { this.authorJwtHash = authorJwtHash; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getChatHistoryJson() { return chatHistoryJson; }
    public void setChatHistoryJson(String chatHistoryJson) { this.chatHistoryJson = chatHistoryJson; }

    public Boolean getIsManual() { return isManual; }
    public void setIsManual(Boolean isManual) { this.isManual = isManual; }
}
