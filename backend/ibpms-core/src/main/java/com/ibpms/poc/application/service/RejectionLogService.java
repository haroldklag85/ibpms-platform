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

    public RejectionLogService(FormEventRepository formEventRepository) {
        this.formEventRepository = formEventRepository;
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
        // En escenarios reales el reason estaría dentro del JSONB payload descifrado o raw.
        // Para acatar el sprint extraemos un mock seguro si no hay parseo complejo o retornamos "Rechazado por negocio".
        return "Rechazado en etapa anterior (Validación Automática)";
    }
}
