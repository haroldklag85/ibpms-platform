package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.AuditLogUseCase;
import com.ibpms.poc.application.port.in.MoveKanbanTaskUseCase;
import com.ibpms.poc.application.port.out.KanbanTaskPort;
import com.ibpms.poc.domain.model.kanban.KanbanState;
import com.ibpms.poc.domain.model.kanban.KanbanTask;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class KanbanTaskService implements MoveKanbanTaskUseCase {

    private final KanbanTaskPort kanbanTaskPort;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditLogUseCase auditLogService;

    public KanbanTaskService(KanbanTaskPort kanbanTaskPort, SimpMessagingTemplate messagingTemplate, AuditLogUseCase auditLogService) {
        this.kanbanTaskPort = kanbanTaskPort;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public void moveTask(UUID taskId, String newStateStr, String blockedReason, String userId) {
        KanbanTask task = kanbanTaskPort.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        if (task.isImmutable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarea en DONE es inmutable");
        }

        KanbanState newState;
        try {
            newState = KanbanState.valueOf(newStateStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + newStateStr);
        }

        try {
            task.validateTransition(newState);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        if (newState == KanbanState.BLOCKED) {
            try {
                task.requireBlockedReason(blockedReason);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        task.setStatus(newState);
        if (newState != KanbanState.BLOCKED) {
            task.setBlockedReason(null);
        }

        kanbanTaskPort.save(task);

        messagingTemplate.convertAndSend("/topic/kanban/" + task.getBoardId() + "/tasks", "TASK_STATE_CHANGED");

        auditLogService.commit(userId, task);
    }
}
