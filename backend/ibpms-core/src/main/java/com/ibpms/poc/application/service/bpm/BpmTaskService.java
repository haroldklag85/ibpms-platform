package com.ibpms.poc.application.service.bpm;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CA-5: Wrapper Crítico de Seguridad (Row-Level Security).
 * Todo controlador o servicio del Workdesk DEBE inyectar esta clase 
 * y JAMÁS usar org.camunda.bpm.engine.TaskService directamente.
 */
@Service
public class BpmTaskService {

    private final TaskService camundaTaskService;

    public BpmTaskService(TaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    /**
     * Devuelve las tareas de Camunda asegurando privacidad transaccional.
     * Forza la evaluación de Assignee OR Candidate Groups.
     *
     * @param userId El ID del usuario consultante (usualmente extraído del JWT).
     * @param userRoles Lista de roles (ej: ROLE_APROBADOR) que el usuario posee.
     * @return Lista de Tareas filtradas rígidamente.
     */
    public List<Task> getSecureUserTasks(String userId, List<String> userRoles) {
        TaskQuery query = camundaTaskService.createTaskQuery().active();
        
        if (userRoles != null && !userRoles.isEmpty()) {
            query.or()
                 .taskAssignee(userId)
                 .taskCandidateGroupIn(userRoles)
                 .endOr();
        } else {
            // Si el usuario no tiene roles estáticos, solo puede ver lo explícitamente asignado.
            query.taskAssignee(userId);
        }

        // CA-13: Roles Dinámicos (Process + Task level variables)
        // Ya que Camunda interpreta los Expression Lanes en Runtime literalizando el Assignee
        // la consulta .taskAssignee() y .taskCandidateGroupIn() nativa ya envuelve
        // a los actores dinámicos inyectados por el motor sin requerir cruces manuales.
        return query.orderByTaskCreateTime().desc().list();
    }

    /**
     * Valida si un usuario tiene permisos reales sobre una tarea específica
     * antes de permitir una operación (Reclaim, Complete, etc).
     */
    public boolean canInteractWithTask(String taskId, String userId, List<String> userRoles) {
        TaskQuery query = camundaTaskService.createTaskQuery().taskId(taskId).active();
        
        if (userRoles != null && !userRoles.isEmpty()) {
            query.or()
                 .taskAssignee(userId)
                 .taskCandidateGroupIn(userRoles)
                 .endOr();
        } else {
            query.taskAssignee(userId);
        }

        return query.count() > 0;
    }
}
