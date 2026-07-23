package com.ibpms.poc.application.port.in;

import java.util.UUID;

import com.ibpms.poc.domain.model.kanban.KanbanTask;

public interface MoveKanbanTaskUseCase {
    KanbanTask moveTask(UUID taskId, String newState, String blockedReason, String userId);
}
