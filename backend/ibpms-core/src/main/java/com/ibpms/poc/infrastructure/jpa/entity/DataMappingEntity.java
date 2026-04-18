package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_data_mappings")
public class DataMappingEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "process_definition_key", nullable = false)
    private String processDefinitionKey;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "connector_id")
    private String connectorId;

    @Column(name = "mapping_json", columnDefinition = "TEXT")
    private String mappingJson;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

    public DataMappingEntity() {
        this.id = UUID.randomUUID();
    }

    public DataMappingEntity(String processDefinitionKey, String taskId, String connectorId, String mappingJson) {
        this();
        this.processDefinitionKey = processDefinitionKey;
        this.taskId = taskId;
        this.connectorId = connectorId;
        this.mappingJson = mappingJson;
        this.lastValidatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
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
