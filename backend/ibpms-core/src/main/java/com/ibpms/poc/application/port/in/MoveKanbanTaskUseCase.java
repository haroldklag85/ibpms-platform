package com.ibpms.poc.application.port.in;

import java.util.UUID;

public interface MoveKanbanTaskUseCase {
    void moveTask(UUID taskId, String newState, String blockedReason, String userId);
}
