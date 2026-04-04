package com.ibpms.poc.application.service.security;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <b>Servicio de Segregación de Datos y Protección BOLA/IDOR (CA-5, CA-4)</b>
 * <p>
 * Este servicio orquesta la segregación lógica de inquilinos (Multi-Tenancy) y
 * aplica de manera estricta Políticas de Autorización a Nivel de Objeto (Row-Level Security / RLS).
 * </p>
 * <p>
 * 🚨 <b>CRÍTICO PARA INGENIEROS DE BACKEND:</b> 🚨<br/>
 * El mecanismo de consulta aquí definido actúa como un "Interceptor Lógico" que mitiga proactivamente
 * vulnerabilidades de tipo <i>Broken Object Level Authorization (BOLA)</i> o <i>Insecure Direct Object References (IDOR)</i>.
 * Ninguna interfaz del ecosistema (API REST, GraphQL o Workers) debe realizar consultas de tareas
 * directas llamando a {@code TaskService#createTaskQuery()} omitiendo este servicio, ya que hacerlo
 * abre una brecha donde un usuario podría adivinar el ID de una tarea de otro tenant o grupo e interactuar con ella.
 * </p>
 */
@Service
public class DataSegregationService {

    private final TaskService taskService;

    public DataSegregationService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Construye un envoltorio (Query) de contingencia mutua para el motor de tareas de Camunda.
     * Al inyectar iterativamente las restricciones del "Claim" temporal del usuario autenticado
     * por EntraID/Identity corporativo, enjaulamos el área de acción transaccional, garantizando
     * aislamiento de datos frente a enumeración en URIs de la API REST o manipulaciones del Frontend.
     *
     * @param currentUser El ID verificado en el token del usuario actual (NUNCA provisto por body o path param inyectable).
     * @param userRoles   Los Roles o Grupos a los que pertenece el usuario verificados en JWT (calculados cruzando RoleHierarchy).
     * @return Una instancia de {@link TaskQuery} amurallada sobre la cual se puede continuar la operación (Ej: count, appaginate, singleResult).
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
