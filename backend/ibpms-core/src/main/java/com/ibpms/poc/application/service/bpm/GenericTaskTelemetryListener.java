package com.ibpms.poc.application.service.bpm;

import com.ibpms.poc.application.event.GenericTaskCompletedEvent;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.GenericTaskLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.bpm.GenericTaskLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class GenericTaskTelemetryListener {

    private final GenericTaskLogRepository logRepository;

    public GenericTaskTelemetryListener(GenericTaskLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * CA-7: Aplanamiento de Logs Genéricos.
     * Escucha asíncronamente para no bloquear el Hilo HTTP ni penalizar el Flush de Camunda.
     */
    @Async
    @EventListener
    @Transactional
    public void handleGenericTaskCompleted(GenericTaskCompletedEvent event) {
        Map<String, Object> variables = event.getSubmittedVariables();

        String comments = "";
        boolean hasEvidence = false;

        if (variables != null) {
            // CA-6: Rastreo dinámico extraído sobre un prefijo o key común.
            // Para ejemplificar interceptaremos cualquier variable que contenga "comment" u "observacion".
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if ((key.contains("comment") || key.contains("observacion")) && entry.getValue() != null) {
                    comments += entry.getKey() + ": " + entry.getValue().toString() + "\n";
                }
                if (key.contains("evidence") || key.contains("adjunto") || key.contains("file")) {
                    hasEvidence = true;
                }
            }
        }

        GenericTaskLogEntity logEntity = new GenericTaskLogEntity(
                event.getTaskId(),
                event.getProcessInstanceId(),
                event.getUserId(),
                comments.trim(),
                hasEvidence
        );

        logRepository.save(logEntity);
    }
}
