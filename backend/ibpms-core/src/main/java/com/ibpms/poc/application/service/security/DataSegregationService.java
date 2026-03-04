package com.ibpms.poc.application.service.security;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de Segregación de Datos (CA-5, CA-4).
 * Enforces Row-Level Security (RLS) on the Workdesk.
 */
@Service
public class DataSegregationService {

    private final TaskService taskService;

    public DataSegregationService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Devuelve una query de tareas de Camunda pre-filtrada de forma segura para el
     * usuario actual.
     * Garantiza que un usuario no pueda consultar colas que no le corresponden.
     *
     * @param currentUser El ID del usuario EntraID o interno (Identity).
     * @param userRoles   Roles o Grupos a los que pertenece el usuario (calculados
     *                    cruzando RoleHierarchy).
     * @return TaskQuery seguro y filtrado.
     */
    public TaskQuery getSecureTaskQuery(String currentUser, List<String> userRoles) {
        TaskQuery query = taskService.createTaskQuery()
                .active();

        if (userRoles != null && !userRoles.isEmpty()) {
            // WHERE assignee = :currentUser OR candidateGroup IN (:userRoles)
            query = query.or()
                    .taskAssignee(currentUser)
                    .taskCandidateGroupIn(userRoles)
                    .endOr();
        } else {
            // WHERE assignee = :currentUser
            query = query.taskAssignee(currentUser);
        }

        return query;
    }
}
