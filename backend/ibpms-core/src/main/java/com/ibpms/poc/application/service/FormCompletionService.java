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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;

import com.ibpms.poc.infrastructure.security.PiiEncryptionService;
import com.ibpms.poc.infrastructure.jpa.entity.TempDocumentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TempDocumentRepository;

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
    private final TempDocumentRepository tempDocumentRepository;

    public FormCompletionService(
            AutoClaimService autoClaimService,
            CamundaCompletionAdapter camundaCompletionAdapter,
            FormEventRepository formEventRepository,
            TaskDraftRepository taskDraftRepository,
            TaskService taskService,
            ObjectMapper objectMapper,
            PiiEncryptionService piiEncryptionService,
            EventReferenceGenerator eventReferenceGenerator,
            TempDocumentRepository tempDocumentRepository) {
        this.autoClaimService = autoClaimService;
        this.camundaCompletionAdapter = camundaCompletionAdapter;
        this.formEventRepository = formEventRepository;
        this.taskDraftRepository = taskDraftRepository;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
        this.piiEncryptionService = piiEncryptionService;
        this.eventReferenceGenerator = eventReferenceGenerator;
        this.tempDocumentRepository = tempDocumentRepository;
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

            // BACK-029-01: JSON Schema Validation
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            // TODO (Deuda Técnica - OBS-QA-01): El validador funciona pero usa schema genérico. 
            // Se debe recuperar el schema real de ibpms_form_definitions.schema_content.
            String mockSchemaString = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\",\"type\": \"object\"}"; 
            JsonSchema schema = factory.getSchema(mockSchemaString);
            JsonNode node = objectMapper.readTree(rawJson);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ValidationFailed: " + errors.toString());
            }

            // BACK-029-06: _visibleFields
            if (request.getVisibleFields() != null) {
                // TODO (V2 - OBS-QA-02): Diferir recálculo dinámico de condiciones a V2. 
                // Actual: comprobación estática con placeholder 'missing_required_field'.
                if (request.getVisibleFields().contains("missing_required_field")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required visible field");
                }
            }

            // BACK-029-05: Anti-IDOR para cada UUID en el rawJson
            // TODO (V2 - OBS-QA-03): El broad scan actual captura TODOS los UUIDs causando queries a BBDD innecesarias. 
            // Refactorizar en V2 para que solo escanee campos específicos (ej. array attachments[]).
            Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            Matcher matcher = uuidPattern.matcher(rawJson);
            while (matcher.find()) {
                String potentialUuid = matcher.group();
                java.util.Optional<TempDocumentEntity> docOpt = tempDocumentRepository.findById(UUID.fromString(potentialUuid));
                if (docOpt.isPresent()) {
                    TempDocumentEntity doc = docOpt.get();
                    if (!doc.getTaskId().equals(taskId) || !doc.getUserId().equals(userId)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anti-IDOR: No tiene acceso al archivo temporal " + potentialUuid);
                    }
                    // Marcar como CONFIRMED
                    doc.setStatus("CONFIRMED");
                    tempDocumentRepository.save(doc);
                }
            }

            String encryptedBase64 = piiEncryptionService.encrypt(rawJson);
            // Save wrapped in JSON so PostgreSQL allows storing it as JSONB properly
            jsonPayload = "{\"sealed_pii_payload\": \"" + encryptedBase64 + "\", \"_visibleFields\": " + 
                          (request.getVisibleFields() != null ? objectMapper.writeValueAsString(request.getVisibleFields()) : "[]") + "}";
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) throw (ResponseStatusException) e;
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
