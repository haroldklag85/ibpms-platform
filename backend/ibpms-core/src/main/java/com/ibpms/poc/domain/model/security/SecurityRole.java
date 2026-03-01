package com.ibpms.poc.domain.model.security;

import java.util.UUID;

public class SecurityRole {
    private UUID id;
    private String name;
    private String description;
    private String type; // GLOBAL, PROCESS_GENERATED
    private String processDefinitionId;
    private String laneId;

    public SecurityRole(UUID id, String name, String description, String type, String processDefinitionId,
            String laneId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.processDefinitionId = processDefinitionId;
        this.laneId = laneId;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getLaneId() {
        return laneId;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }
}
