package com.ibpms.poc.infrastructure.jpa.entity.security;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ibpms_security_process_permission")
public class ProcessPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "process_definition_key", nullable = false, length = 150)
    private String processDefinitionKey;

    @Column(name = "can_initiate_process", nullable = false)
    private Boolean canInitiateProcess = false;

    @Column(name = "can_execute_tasks", nullable = false)
    private Boolean canExecuteTasks = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    public ProcessPermissionEntity() {}

    public ProcessPermissionEntity(String processDefinitionKey, Boolean canInitiateProcess, Boolean canExecuteTasks, RoleEntity role) {
        this.processDefinitionKey = processDefinitionKey;
        this.canInitiateProcess = canInitiateProcess;
        this.canExecuteTasks = canExecuteTasks;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }
    public Boolean getCanInitiateProcess() { return canInitiateProcess; }
    public void setCanInitiateProcess(Boolean canInitiateProcess) { this.canInitiateProcess = canInitiateProcess; }
    public Boolean getCanExecuteTasks() { return canExecuteTasks; }
    public void setCanExecuteTasks(Boolean canExecuteTasks) { this.canExecuteTasks = canExecuteTasks; }
    public RoleEntity getRole() { return role; }
    public void setRole(RoleEntity role) { this.role = role; }
}
