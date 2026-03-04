package com.ibpms.poc.application.service.inbox;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de abstraer las consultas hacia el inbox unificado
 * (Pantalla 14).
 * Su responsabilidad clave es garantizar la Segregación de Datos: Ningún
 * usuario
 * debe poder consultar tareas asignadas exclusivamente a otro, a menos que
 * asuma el
 * rol de Administrador.
 */
@Service
public class TaskQueryService {

    private final TaskService camundaTaskService;

    public TaskQueryService(TaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    /**
     * Consulta las tareas activas PERO restringe forzosamente los resultados
     * a aquellas que estén explícitamente asignadas (o reclamables) por el usuario.
     * 
     * @param userId Identificador único extraído nativamente desde el JWT.
     * @return Lista de Tareas purgada de información segregada.
     */
    public List<Task> getSegregatedInboxTasks(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new SecurityException("Identidad No Determinada: No se puede segregar el inbox sin un UserId.");
        }

        // Camunda API Query: La segregación ocurre a nivel de BDD Engine.
        // Solo retorna donde el 'taskAssignee' = userId
        // (En V2 de iBPMS se combinará con InCandidateGroup para RBAC avanzado).
        return camundaTaskService.createTaskQuery()
                .taskAssignee(userId)
                .active()
                .orderByTaskCreateTime().desc()
                .list();
    }
}
