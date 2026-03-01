package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.util.UUID;

@Entity
@Table(name = "sys_role")
public class SysRoleEntity {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "process_definition_id", length = 100)
    private String processDefinitionId;

    @Column(name = "lane_id", length = 100)
    private String laneId;

    // Constructors
    public SysRoleEntity() {
    }

    public SysRoleEntity(UUID id, String name, String description, String type, String processDefinitionId,
            String laneId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.processDefinitionId = processDefinitionId;
        this.laneId = laneId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }
}
