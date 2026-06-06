package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.port.FormEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RejectionLogService {

    private final FormEventRepository formEventRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public RejectionLogService(FormEventRepository formEventRepository, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.formEventRepository = formEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Devuelve el historial de rechazos de una instancia de proceso,
     * formateado para inyección directa en el BFF.
     */
    public List<Map<String, Object>> getRejectionHistory(String processInstanceId) {
        List<FormEvent> rejectionEvents = formEventRepository
                .findByProcessInstanceIdAndEventType(processInstanceId, EventType.FORM_REJECTED);

        return rejectionEvents.stream()
                .map(event -> Map.<String, Object>of(
                        "eventId", event.getEventId().toString(),
                        "timestamp", event.getCreatedAt().toString(),
                        "userId", event.getUserId(),
                        "taskId", event.getTaskId(),
                        "reason", extractRejectionReason(event)
                ))
                .collect(Collectors.toList());
    }

    private String extractRejectionReason(FormEvent event) {
        if (event.getPayloadJson() == null || event.getPayloadJson().isBlank()) {
            return "Rechazado en etapa anterior (Validación Automática)";
        }
        try {
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(event.getPayloadJson());
            if (rootNode.has("reason")) {
                return rootNode.get("reason").asText();
            }
        } catch (Exception e) {
            // Ignore parse errors and return fallback
        }
        return "Rechazado en etapa anterior (Validación Automática)";
    }
}
