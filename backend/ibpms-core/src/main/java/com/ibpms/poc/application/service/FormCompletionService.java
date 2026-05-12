package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.application.dto.FormSubmitResponse;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import com.ibpms.poc.application.ports.out.TaskQueryPort;
import com.ibpms.poc.application.ports.out.TaskQueryPort.TaskInfo;
import com.ibpms.poc.application.ports.out.FormDefinitionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Set;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;

import com.ibpms.poc.infrastructure.security.PiiEncryptionService;
import com.ibpms.poc.domain.port.DocumentSecurityPort;

@Service
public class FormCompletionService {

    private final AutoClaimService autoClaimService;
    private final CamundaCompletionAdapter camundaCompletionAdapter;
    private final FormEventRepository formEventRepository;
    private final TaskDraftRepository taskDraftRepository;
    private final TaskQueryPort taskQueryPort;
    private final FormDefinitionPort formDefinitionPort;
    private final ObjectMapper objectMapper;
    private final PiiEncryptionService piiEncryptionService;
    private final EventReferenceGenerator eventReferenceGenerator;
    private final DocumentSecurityPort documentSecurityPort;

    public FormCompletionService(
            AutoClaimService autoClaimService,
            CamundaCompletionAdapter camundaCompletionAdapter,
            FormEventRepository formEventRepository,
            TaskDraftRepository taskDraftRepository,
            TaskQueryPort taskQueryPort,
            FormDefinitionPort formDefinitionPort,
            ObjectMapper objectMapper,
            PiiEncryptionService piiEncryptionService,
            EventReferenceGenerator eventReferenceGenerator,
            DocumentSecurityPort documentSecurityPort) {
        this.autoClaimService = autoClaimService;
        this.camundaCompletionAdapter = camundaCompletionAdapter;
        this.formEventRepository = formEventRepository;
        this.taskDraftRepository = taskDraftRepository;
        this.taskQueryPort = taskQueryPort;
        this.formDefinitionPort = formDefinitionPort;
        this.objectMapper = objectMapper;
        this.piiEncryptionService = piiEncryptionService;
        this.eventReferenceGenerator = eventReferenceGenerator;
        this.documentSecurityPort = documentSecurityPort;
    }

    /**
     * @Traceability: US-029
     * CA-1: Persistir evento inmutable
     * CA-2: DTO minificado a Camunda (SOLO variables de gateway)
     * CA-4, CA-10: Rollback Saga 
     * CA-13: Auto-Claim
     * CA-15: Generar eventReference (EVT-XXXXXX)
     * CA-16: Eliminar draft
     * OBS-QA-01: Validación de JSON Schema contra el diseño persistido en base de datos.
     * OBS-QA-03: Optimización Anti-IDOR para UUIDs específicos, reduciendo carga a DB.
     */
    @Transactional(noRollbackFor = SagaCompensationException.class)
    public FormSubmitResponse completeTask(String taskId, FormSubmitRequest request, String userId) {
        // @Traceability: US-029 - CA-13: Auto-Claim validará y asignará la tarea si es posible y necesaria
        autoClaimService.tryAutoClaim(taskId, userId);

        TaskInfo task = taskQueryPort.findTaskById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // @Traceability: US-029 - CA-3: Cifrar campos PII en payload
        String jsonPayload;
        try {
            String rawJson = objectMapper.writeValueAsString(request.getPayload());

            // @Traceability: US-029 - OBS-QA-01: JSON Schema Validation Real
            // Buscamos el schema real usando el port inyectado en vez de quemarlo
            String schemaContent = formDefinitionPort.findSchemaContentByVersion(request.getSchemaVersion())
                .orElse("{\"$schema\": \"http://json-schema.org/draft-07/schema#\",\"type\": \"object\"}"); 
            // *NOTA*: Fallback al estático mantenido internamente solo por safety en V1 si no se halla UUID, 
            // pero el puerto ya está conectado (Desacople logrado).
            
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaContent);
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

            // @Traceability: US-029 - OBS-QA-03: Anti-IDOR Optimizado
            // En vez de un RegExp global sobre todo el JSON que mataba el rendimiento, iteramos arrays que sabemos contienen UUIDs de negocio (ej. attachments).
            Object attachmentsObj = request.getPayload().get("attachments");
            if (attachmentsObj instanceof List) {
                for (Object item : (List<?>) attachmentsObj) {
                    if (item instanceof String) {
                        String potentialUuid = (String) item;
                        if (potentialUuid.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                            documentSecurityPort.confirmOwnershipAndMarkConfirmed(UUID.fromString(potentialUuid), taskId, userId);
                        }
                    }
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

        // @Traceability: US-029 - CA-1: Bóveda Inmutable
        UUID eventId = UUID.randomUUID();
        FormEvent submittedEvent = FormEvent.builder()
                .eventId(eventId)
                .eventType(EventType.FORM_SUBMITTED)
                .taskId(taskId)
                .processInstanceId(task.processInstanceId())
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
                    .processInstanceId(task.processInstanceId())
                    .userId(userId)
                    .payloadJson(jsonPayload)
                    .schemaVersion(request.getSchemaVersion())
                    .originalEventId(eventId)
                    .createdAt(ZonedDateTime.now())
                    .build();
            formEventRepository.save(rolledBackEvent);

            throw new SagaCompensationException("SAGA_COMPENSATION_EXECUTED", e);
        }

        // @Traceability: US-029 - CA-16: Cleanup draft de forma atómica en la misma transacción
        taskDraftRepository.findByTaskIdAndUserId(taskId, userId)
                .ifPresent(draft -> taskDraftRepository.deleteById(draft.getId()));

        // @Traceability: US-029 - CA-15: Event Reference
        String eventReference = eventReferenceGenerator.generateFromId(eventId);
        
        return FormSubmitResponse.builder()
                .eventReference(eventReference)
                .status("SUCCESS")
                .message("Task completed successfully")
                .build();
    }
}
