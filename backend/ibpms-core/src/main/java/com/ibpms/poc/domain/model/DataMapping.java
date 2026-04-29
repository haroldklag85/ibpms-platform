package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class DataMapping {
    private UUID id;
    private String processDefinitionKey;
    private String taskId;
    private String connectorId;
    private String mappingJson;
    private LocalDateTime lastValidatedAt;

    public DataMapping() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getMappingJson() { return mappingJson; }
    public void setMappingJson(String mappingJson) { this.mappingJson = mappingJson; }
    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
}
