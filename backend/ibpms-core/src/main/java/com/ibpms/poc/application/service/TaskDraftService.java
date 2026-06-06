package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.application.port.out.AgileTaskPort;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.time.ZonedDateTime;

/**
 * US-029 / US-003: Persistencia progresiva de Borradores y Validación de Completitud (CA-91).
 */
@Service
@Traceability(US = "US-003", CA = {"CA-91"})
public class TaskDraftService {

    private final AgileTaskPort taskRepository;
    private final ObjectMapper objectMapper;
    // Asumimos un puerto o invocación directa a CamundaTaskService para completar.
    // Omito la inyección real de Camunda por abstracción, pero preparo la firma.

    public TaskDraftService(AgileTaskPort taskRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveDraft(UUID taskId, Map<String, Object> payload, String savedBy) {
        AgileTask task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!"CLAIMED".equals(task.getStatus()) && !"DRAFT".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden guardar borradores en tareas CLAIMED o DRAFT");
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String currentHash = DigestUtils.md5DigestAsHex(jsonPayload.getBytes());

            // Debounce: Ignorar si el hash es idéntico al draft actual
            if (currentHash.equals(task.getDraftPayloadHash())) {
                return; // No hay cambios reales, ahorramos escritura
            }

            task.setDraftPayload(jsonPayload);
            task.setDraftPayloadHash(currentHash);
            task.setStatus("DRAFT");
            
            // BACK-029-08: Draft TTL Auto-Calculation (GAP-16, CA-26)
            ZonedDateTime draftExpiresAt = ZonedDateTime.now().plusHours(72);
            task.setDraftExpiresAt(draftExpiresAt);
            
            taskRepository.save(task);

        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido", e);
        }
    }

    @Transactional(readOnly = true)
    // @Traceability(US="US-003", CA="CA-91", DESC="Recuperación de Borrador CQRS")
    public Map<String, Object> getDraft(UUID taskId) {
        AgileTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (task.getDraftPayload() == null) {
            return java.util.Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(task.getDraftPayload(), Map.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al deserializar borrador", e);
        }
    }

    @Transactional
    // @Traceability(US="US-003", CA="CA-91", DESC="Purgar borrador post-submit")
    public void deleteDraft(UUID taskId) {
        AgileTask task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setDraftPayload(null);
        task.setDraftPayloadHash(null);
        task.setDraftExpiresAt(null);
        taskRepository.save(task);
    }

    @Transactional
    public void completeTask(UUID taskId, Map<String, Object> payload, String completedBy) {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-72: Delegar al método con If-Match
        completeTask(taskId, payload, completedBy, null);
    }

    @Transactional
    public void completeTask(UUID taskId, Map<String, Object> payload, String completedBy, String ifMatch) {
        AgileTask task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!"CLAIMED".equals(task.getStatus()) && !"DRAFT".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estado inválido para completar. Debe ser CLAIMED o DRAFT");
        }

        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-72: Validar bloqueo optimista
        if (ifMatch != null) {
            String currentHash = DigestUtils.md5DigestAsHex((task.getId() + "_" + task.getStatus() + "_" + task.getUpdatedAt().toInstant().toEpochMilli()).getBytes());
            if (!ifMatch.equals(currentHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflicto de versiones detectado");
            }
        }

        // Aquí iría la validación Zod/Server-side del esquema antes de completar (US-029 CA-24)
        // Por brevedad, simularemos que es válido si payload no es nulo o vacío
        if (payload == null || payload.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El payload de completitud está vacío");
        }

        task.setStatus("COMPLETED");
        // Delegaríamos también a Camunda internamente: camundaTaskService.complete(taskId, vars)
        
        taskRepository.save(task);
    }
}
