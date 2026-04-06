package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ibpms_data_mappings")
public class DataMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_definition_key", nullable = false)
    private String processDefinitionKey;

    @Column(name = "variable_name", nullable = false)
    private String variableName;

    @Column(name = "variable_type", nullable = false)
    private String variableType;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public DataMappingEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public DataMappingEntity(String processDefinitionKey, String variableName, String variableType, boolean isRequired) {
        this();
        this.processDefinitionKey = processDefinitionKey;
        this.variableName = variableName;
        this.variableType = variableType;
        this.isRequired = isRequired;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
