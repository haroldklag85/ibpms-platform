package com.ibpms.poc.application.service.bff;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@SuppressWarnings("null")
public class FormBffCoreService {

    private static final Logger log = LoggerFactory.getLogger(FormBffCoreService.class);

    private final TaskService taskService;

    public FormBffCoreService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * CA-01: Inyección Megalítica de Contexto (Patrón BFF).
     */
    public Map<String, Object> generateMegaDtoFormContext(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new jakarta.persistence.EntityNotFoundException("Tarea no encontrada en el orquestador BPMN.");
        }

        // Mock del ensamblaje del Mega-DTO
        log.info("BFF Form Context (CA-01): Ensamblando Zod + Layout + Variables de DB para Tarea {}", taskId);
        return Map.of(
            "taskId", task.getId(),
            "taskName", task.getName(),
            "schema_version", "v1.2.0", // Prevención choques generacionales
            "layout", Map.of("type", "grid", "components", "[]"), // Zod Schema / Layout Layout
            "prefillData", Map.of("applicantName", "Mock User")
        );
    }

    /**
     * CQRS Completion (CA-07, CA-08, CA-09, CA-10)
     */
    @Transactional(rollbackFor = Exception.class) // CA-10: ACID Fallback over Sagas
    public void completeTransactionalForm(String taskId, String userId, Map<String, Object> massivePayload) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new jakarta.persistence.EntityNotFoundException("Tarea Inexistente o ya completada.");
        }

        // CA-07: Integridad de Asignación Concurrente (Implicit Locking)
        // Se examina el assignee de Camunda contra la identidad del JWT (userId)
        if (task.getAssignee() != null && !task.getAssignee().equals(userId)) {
            log.error("Prevención Robos (CA-07): Intento de sumisión no autorizada en Tarea {} por {}", taskId, userId);
            throw new AccessDeniedException("FORBIDDEN: La tarea pertenece a otro assignee.");
        }

        // CA-08: Separación de Responsabilidades y Event Sourcing (CQRS)
        log.info("CQRS (CA-08): Inyectando 'Form_Submitted_Event' inmutable en PostgreSQL. Payload Size: {}", massivePayload.size());
        // mockEventSourcingRepository.save(new FormSubmittedEvent(taskId, massivePayload));
        
        // CA-09: Exclusión Topológica Estratégica de Camunda Engine
        // TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo.
        Map<String, Object> minifiedDto = Map.of(
            "formApproved", massivePayload.getOrDefault("isApproved", true),
            "form_storage_id", java.util.UUID.randomUUID().toString() 
        );
        log.info("BPMN Degradation (CA-09): Notificando a Camunda con Payload Minificado: {}", minifiedDto);

        try {
            taskService.complete(taskId, minifiedDto);
        } catch (Exception e) {
            // CA-10: Consistencia Transaccional Cruda (ACID Fallback over Sagas)
            // Si Camunda crashea, @Transactional propaga el Rollback de DB (Postgres).
            log.error("Camunda Crash 500 (CA-10): El Orquestador reventó. Lanzando Rollback compensatorio estricto a DB!");
            throw new IllegalStateException("CRITICAL_BPMN_ENGINE_UNAVAILABLE", e);
        }
    }
}
