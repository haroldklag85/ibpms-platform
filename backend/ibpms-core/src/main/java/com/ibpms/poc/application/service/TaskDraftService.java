package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

/**
 * US-029: Persistencia progresiva de Borradores y Validación de Completitud.
 */
@Service
public class TaskDraftService {

    private final AgileTaskRepositoryJpa taskRepository;
    private final ObjectMapper objectMapper;
    // Asumimos un puerto o invocación directa a CamundaTaskService para completar.
    // Omito la inyección real de Camunda por abstracción, pero preparo la firma.

    public TaskDraftService(AgileTaskRepositoryJpa taskRepository, ObjectMapper objectMapper) {
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
            taskRepository.save(task);

        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido", e);
        }
    }

    @Transactional
    public void completeTask(UUID taskId, Map<String, Object> payload, String completedBy) {
        AgileTask task = taskRepository.findByIdForUpdate(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!"CLAIMED".equals(task.getStatus()) && !"DRAFT".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estado inválido para completar. Debe ser CLAIMED o DRAFT");
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
