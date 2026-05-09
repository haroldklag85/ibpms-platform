package com.ibpms.poc.application.dto;

/**
 * @Traceability(US = "US-001", CA = {"CA-27"}) Vocabulario estandarizado de acciones WebSocket para el Workdesk.
 * Payload atómico (CA-13) — solo instrucción + ID + payload parcial opcional.
 */
public class WsWorkdeskEventDTO {

    public enum Action {
        REMOVE,            // Tarea reclamada o reasignada
        ADD,               // Nueva tarea asignada al usuario
        UPDATE,            // Campo visible cambió (status, SLA, etc.)
        PRIORITY_CHANGE    // Reordenamiento por impacto financiero
    }

    private Action action;
    private String taskId;
    private String tenantId;
    private WorkdeskGlobalItemDTO payload; // Parcial, solo para ADD y UPDATE

    // Getters y Setters
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public WorkdeskGlobalItemDTO getPayload() { return payload; }
    public void setPayload(WorkdeskGlobalItemDTO payload) { this.payload = payload; }
}
