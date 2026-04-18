package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.application.dto.FormSubmitResponse;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.ibpms.poc.infrastructure.security.PiiEncryptionService;

@Service
public class FormCompletionService {

    private final AutoClaimService autoClaimService;
    private final CamundaCompletionAdapter camundaCompletionAdapter;
    private final FormEventRepository formEventRepository;
    private final TaskDraftRepository taskDraftRepository;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final PiiEncryptionService piiEncryptionService;
    private final EventReferenceGenerator eventReferenceGenerator;

    public FormCompletionService(
            AutoClaimService autoClaimService,
            CamundaCompletionAdapter camundaCompletionAdapter,
            FormEventRepository formEventRepository,
            TaskDraftRepository taskDraftRepository,
            TaskService taskService,
            ObjectMapper objectMapper,
            PiiEncryptionService piiEncryptionService,
            EventReferenceGenerator eventReferenceGenerator) {
        this.autoClaimService = autoClaimService;
        this.camundaCompletionAdapter = camundaCompletionAdapter;
        this.formEventRepository = formEventRepository;
        this.taskDraftRepository = taskDraftRepository;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
        this.piiEncryptionService = piiEncryptionService;
        this.eventReferenceGenerator = eventReferenceGenerator;
    }

    /**
     * CA-01: Persistir evento inmutable
     * CA-02: DTO minificado a Camunda (SOLO variables de gateway)
     * CA-03 + CA-10: Rollback Saga 
     * CA-04 + CA-13: Auto-Claim
     * CA-15: Generar eventReference (EVT-XXXXXX)
     * CA-16: Eliminar draft
     */
    @Transactional
    public FormSubmitResponse completeTask(String taskId, FormSubmitRequest request, String userId) {
        // 1. Auto-Claim validará y asignará la tarea si es posible y necesaria (CA-04, CA-13)
        autoClaimService.tryAutoClaim(taskId, userId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        // CA-12 Cifrar campos PII en payload
        String jsonPayload;
        try {
            String rawJson = objectMapper.writeValueAsString(request.getPayload());
            String encryptedBase64 = piiEncryptionService.encrypt(rawJson);
            // Save wrapped in JSON so PostgreSQL allows storing it as JSONB properly
            jsonPayload = "{\"sealed_pii_payload\": \"" + encryptedBase64 + "\"}";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid payload format", e);
        }

        // 4. Bóveda Inmutable (CA-01)
        UUID eventId = UUID.randomUUID();
        FormEvent submittedEvent = FormEvent.builder()
                .eventId(eventId)
                .eventType(EventType.FORM_SUBMITTED)
                .taskId(taskId)
                .processInstanceId(task.getProcessInstanceId())
                .userId(userId)
                .payloadJson(jsonPayload)
                .schemaVersion(request.getSchemaVersion())
                .idempotencyKey(request.getIdempotencyKey())
                .createdAt(ZonedDateTime.now())
                .build();
        
        formEventRepository.save(submittedEvent);

        // 5 & 6. Delegar a Camunda con Adapter (Retryable 3 veces)
        try {
            camundaCompletionAdapter.completeTaskInEngine(taskId, request.getGatewayVariables());
        } catch (Exception e) {
            // Rollback Saga
            FormEvent rolledBackEvent = FormEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(EventType.FORM_SUBMIT_ROLLED_BACK)
                    .taskId(taskId)
                    .processInstanceId(task.getProcessInstanceId())
                    .userId(userId)
                    .payloadJson(jsonPayload)
                    .schemaVersion(request.getSchemaVersion())
                    .originalEventId(eventId)
                    .createdAt(ZonedDateTime.now())
                    .build();
            formEventRepository.save(rolledBackEvent);

            throw new SagaCompensationException("SAGA_COMPENSATION_EXECUTED", e);
        }

        // 7. Cleanup draft de forma atómica en la misma transacción (CA-16)
        taskDraftRepository.findByTaskIdAndUserId(taskId, userId)
                .ifPresent(draft -> taskDraftRepository.deleteById(draft.getId()));

        // 8. Event Reference
        String eventReference = eventReferenceGenerator.generateFromId(eventId);
        
        return FormSubmitResponse.builder()
                .eventReference(eventReference)
                .status("SUCCESS")
                .message("Task completed successfully")
                .build();
    }
}
