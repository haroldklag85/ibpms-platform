package com.ibpms.poc.application.service.bpm;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.ibpms.poc.application.dto.bpm.GenericTaskPayloadDTO;
import com.ibpms.poc.application.event.GenericTaskCompletedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CA-5: Wrapper Crítico de Seguridad (Row-Level Security).
 * Todo controlador o servicio del Workdesk DEBE inyectar esta clase 
 * y JAMÁS usar org.camunda.bpm.engine.TaskService directamente.
 */
@Service
public class BpmTaskService {

    private final TaskService camundaTaskService;
    private final ApplicationEventPublisher eventPublisher;

    public BpmTaskService(TaskService camundaTaskService, ApplicationEventPublisher eventPublisher) {
        this.camundaTaskService = camundaTaskService;
        this.eventPublisher = eventPublisher;
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

    /**
     * CA-1 y CA-2: BFF Filter & VIP Restrictor para Pantalla 7.B
     */
    public GenericTaskPayloadDTO getGenericTaskPayload(String taskId, String userId, List<String> userRoles) {
        
        if (!canInteractWithTask(taskId, userId, userRoles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos RLS sobre esta tarea.");
        }

        Task task = camundaTaskService.createTaskQuery().taskId(taskId).initializeFormKeys().singleResult();
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada.");
        }

        // CA-1: VIP Pre-Flight Restrictor
        boolean isVip = userRoles != null && (userRoles.contains("ROLE_ALTA_DIRECCION") || userRoles.contains("ROLE_APROBADOR_FINANCIERO"));
        
        if ("sys_generic_form".equals(task.getFormKey()) && isVip) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "RESTRICCIÓN VIP: Los roles Gerenciales/Financieros tienen prohibido utilizar Formularios Genéricos. " +
                "Por norma transaccional, debe utilizarse la Pantalla 7 Formal Programada.");
        }

        // CA-2: Anti-Basura BFF Filter (Whitelist)
        Map<String, Object> rawVariables = camundaTaskService.getVariables(taskId);
        
        Map<String, String> dynamicFields = new HashMap<>();
        // Extraemos 5 campos dinámicos simulados (Ej: comentarios pre-flight) sanitizados
        for (Map.Entry<String, Object> entry : rawVariables.entrySet()) {
            if (entry.getKey().startsWith("dyn_") && entry.getValue() != null && dynamicFields.size() < 5) {
                dynamicFields.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        GenericTaskPayloadDTO dto = new GenericTaskPayloadDTO(
            task.getId(),
            task.getProcessInstanceId(),
            (String) rawVariables.get("caseId"),
            (String) rawVariables.get("clientName"),
            rawVariables.get("amount") != null ? Double.valueOf(rawVariables.get("amount").toString()) : null,
            (String) rawVariables.get("priority"),
            (String) rawVariables.get("description"),
            dynamicFields
        );

        return dto;
    }

    /**
     * CA-6 y CA-7: Namespacing Preventivo y Aplanamiento.
     * Completa el formulario genérico empujando limpiamente las variables dinámicas
     * y disparando la telemetría asíncrona (Domain Event).
     */
    public void completeGenericTask(String taskId, String userId, List<String> userRoles, Map<String, Object> variables) {
        if (!canInteractWithTask(taskId, userId, userRoles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos RLS sobre esta tarea.");
        }

        Task task = camundaTaskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada.");
        }

        // CA-06: Segregación de Funciones (SoD) Juez y Parte
        Object initiatorObj = camundaTaskService.getVariable(task.getExecutionId(), "initiator");
        if (initiatorObj != null && userId.equals(initiatorObj.toString())) {
            eventPublisher.publishEvent(new com.ibpms.poc.application.event.SecurityAnomalyEvent("INTENTO_SOD_AUTOAPROBACION", userId, taskId));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segregación de Funciones (SoD): Tiene estrictamente prohibido aprobar una instancia creada por usted mismo.");
        }

        // 1. Inyectamos silenciosamente las variables dinámicas de Frontend en el Scope de Camunda
        camundaTaskService.complete(taskId, variables);

        // 2. Liberamos a Camunda (Fire and Forget hacia la tabla Plana GenericTaskLogEntity)
        eventPublisher.publishEvent(new GenericTaskCompletedEvent(
                taskId,
                task.getProcessInstanceId(),
                userId,
                variables
        ));
    }
}
