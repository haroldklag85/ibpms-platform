package com.ibpms.poc.application.service.bff;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.service.RejectionLogService;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;

import java.time.Instant;
import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.application.port.out.FormDesignPort;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.domain.model.FormDefinition;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class FormBffCoreService {

    private static final Logger log = LoggerFactory.getLogger(FormBffCoreService.class);

    private final TaskService taskService;
    private final RejectionLogService rejectionLogService;
    private final FormEventRepository formEventRepository;
    private final ObjectMapper objectMapper;
    private final FormDefinitionPort formDefinitionPort;
    private final FormDesignPort formDesignPort;

    public FormBffCoreService(TaskService taskService, 
                              RejectionLogService rejectionLogService,
                              FormEventRepository formEventRepository,
                              ObjectMapper objectMapper,
                              FormDefinitionPort formDefinitionPort,
                              FormDesignPort formDesignPort) {
        this.taskService = taskService;
        this.rejectionLogService = rejectionLogService;
        this.formEventRepository = formEventRepository;
        this.objectMapper = objectMapper;
        this.formDefinitionPort = formDefinitionPort;
        this.formDesignPort = formDesignPort;
    }

    /**
     * CA-01 / CA-81: Inyección Megalítica de Contexto (Patrón BFF) con Anclaje de Versión In-Flight.
     * // @Traceability: US-003 - CA-81
     */
    public Map<String, Object> generateMegaDtoFormContext(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new jakarta.persistence.EntityNotFoundException("Tarea no encontrada en el orquestador BPMN.");
        }

        // Recuperar historial de rechazos si lo hubiera
        List<Map<String, Object>> rejectionHistory = rejectionLogService.getRejectionHistory(task.getProcessInstanceId());

        // Conectar form prefillData con las variables reales de Camunda para resolver (B-J04-02)
        Map<String, Object> realVariables = taskService.getVariables(taskId);

        // CA-81: Anclaje de Versión para Procesos In-Flight (Lazy Patching)
        String formKey = task.getFormKey();
        UUID formId = null;
        if (formKey != null) {
            try {
                formId = UUID.fromString(formKey);
            } catch (IllegalArgumentException e) {
                // Si es un technicalName, buscamos el formId correspondiente
                Optional<FormDesignDTO> designOpt = formDesignPort.findTopByTechnicalNameOrderByVersionDesc(formKey);
                if (designOpt.isPresent()) {
                    formId = designOpt.get().getId();
                }
            }
        }

        FormDefinition anchoredDef = null;
        if (formId != null) {
            List<FormDefinition> versions = formDefinitionPort.findByFormIdOrderByVersionIdDesc(formId);
            Date taskCreateTime = task.getCreateTime();
            if (taskCreateTime != null && !versions.isEmpty()) {
                Instant taskInstant = taskCreateTime.toInstant();
                for (FormDefinition def : versions) {
                    if (def.getCreatedAt() != null) {
                        Instant defInstant = def.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
                        if (!defInstant.isAfter(taskInstant)) {
                            anchoredDef = def;
                            break;
                        }
                    }
                }
            }
            // Fallback: si no hay versión antes del taskCreateTime, tomar la versión más antigua (primera versión)
            if (anchoredDef == null && !versions.isEmpty()) {
                anchoredDef = versions.get(versions.size() - 1);
            }
        }

        Map<String, Object> layoutMap = null;
        String schemaVer = "v1.2.0";
        if (anchoredDef != null) {
            schemaVer = String.valueOf(anchoredDef.getVersionId());
            try {
                layoutMap = objectMapper.readValue(anchoredDef.getSchemaContent(), Map.class);
            } catch (Exception e) {
                log.error("Failed to parse anchored form schema JSON (CA-81)", e);
            }
        }

        if (layoutMap == null) {
            layoutMap = Map.of("type", "grid", "components", "[]");
        }

        log.info("BFF Form Context (CA-01/CA-81): Ensamblando contexto real para Tarea {} con versión {}", taskId, schemaVer);
        return Map.of(
            "taskId", task.getId(),
            "taskName", task.getName(),
            "schema_version", schemaVer,
            "layout", layoutMap,
            "prefillData", realVariables.isEmpty() ? Map.of() : realVariables,
            "rejection_history", rejectionHistory
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
        
        try {
            String payloadStr = objectMapper.writeValueAsString(massivePayload);
            FormEvent event = FormEvent.builder()
                .eventId(java.util.UUID.randomUUID())
                .eventType(EventType.FORM_SUBMITTED)
                .taskId(taskId)
                .processInstanceId(task.getProcessInstanceId())
                .userId(userId)
                .payloadJson(payloadStr)
                .schemaVersion("v1.2.0")
                .createdAt(java.time.ZonedDateTime.now())
                .build();
            formEventRepository.save(event);
        } catch (Exception e) {
            throw new IllegalStateException("Error persistiendo evento CQRS", e);
        }
        
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
