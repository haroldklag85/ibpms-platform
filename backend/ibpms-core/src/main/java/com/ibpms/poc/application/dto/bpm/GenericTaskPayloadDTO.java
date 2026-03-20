package com.ibpms.poc.application.dto.bpm;

import java.util.Map;

/**
 * BFF Whitelist DTO (CA-2).
 * Excluye explícitamente el "Context Bleeding" (variables técnicas, UUIDs de Camunda, etc.).
 * Únicamente viajan al Frontend los metadatos visuales del Formulario Genérico.
 */
public class GenericTaskPayloadDTO {
    
    private String taskId;
    private String processInstanceId;
    private String caseId;
    private String clientName;
    private Double amount;
    private String priority;
    private String description;
    // Espacio para 5 variables dinámicas seguras (Whitelisted strings)
    private Map<String, String> dynamicFields;

    public GenericTaskPayloadDTO() {}

    public GenericTaskPayloadDTO(String taskId, String processInstanceId, String caseId, String clientName, Double amount, String priority, String description, Map<String, String> dynamicFields) {
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.caseId = caseId;
        this.clientName = clientName;
        this.amount = amount;
        this.priority = priority;
        this.description = description;
        this.dynamicFields = dynamicFields;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getDynamicFields() { return dynamicFields; }
    public void setDynamicFields(Map<String, String> dynamicFields) { this.dynamicFields = dynamicFields; }
}
