// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.port.out;

import java.util.Optional;

/**
 * Puerto de salida para buscar información de tareas del motor BPMN.
 * @Traceability: US-029 - Aislar dependencias directas a Camunda TaskService.
 */
public interface TaskQueryPort {
    
    Optional<TaskInfo> findTaskById(String taskId);

    // DTO interno para no depender de Camunda
    record TaskInfo(String id, String processInstanceId, String assignee) {}
}
