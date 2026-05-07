package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.TrackTimeUseCase;
import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.application.port.out.TimeLogPort;
import com.ibpms.poc.domain.model.kanban.KanbanState;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import com.ibpms.poc.domain.model.kanban.TimeLogEntry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.ibpms.poc.crosscutting.annotations.Traceability;

@Service
@Traceability(US = "US-008", CA = {"CA-09"})
public class TimeTrackingService implements TrackTimeUseCase {

    private final TimeLogPort timeLogPort;
    private final KanbanTaskPort kanbanTaskPort;

    public TimeTrackingService(TimeLogPort timeLogPort, KanbanTaskPort kanbanTaskPort) {
        this.timeLogPort = timeLogPort;
        this.kanbanTaskPort = kanbanTaskPort;
    }

    @Override
    @Transactional
    public TimeLogEntry startTimer(UUID referenceId, String referenceType, String userId) {
        if (!"TASK_AGILE".equals(referenceType) && !"TASK_BPMN".equals(referenceType) && !"TASK_GANTT".equals(referenceType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "referenceType no válido. Solo se permiten TASK_BPMN, TASK_AGILE, TASK_GANTT");
        }

        if ("TASK_AGILE".equals(referenceType)) {
            KanbanTask task = kanbanTaskPort.findById(referenceId).orElse(null);
            if (task != null) {
                if (task.getStatus() == KanbanState.TODO || task.getStatus() == KanbanState.DONE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede iniciar el timer en estado TODO o DONE");
                }
            }
        }

        if (timeLogPort.findActiveByUserAndReference(userId, referenceId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un timer activo para este usuario y tarea");
        }

        TimeLogEntry entry = new TimeLogEntry(
                UUID.randomUUID(), referenceId, referenceType, ZonedDateTime.now(), null, null, userId, ZonedDateTime.now()
        );
        return timeLogPort.save(entry);
    }

    @Override
    @Transactional
    public TimeLogEntry stopTimer(UUID logId, String userId) {
        TimeLogEntry entry = timeLogPort.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log de tiempo no encontrado"));

        if (!entry.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede detener el timer de otro usuario");
        }

        try {
            entry.stop(ZonedDateTime.now());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        
        return timeLogPort.save(entry);
    }
}
