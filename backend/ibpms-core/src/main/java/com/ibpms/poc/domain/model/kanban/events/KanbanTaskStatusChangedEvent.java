package com.ibpms.poc.domain.model.kanban.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Evento emitido cuando una tarjeta Kanban cambia de estado (CA-07).
 */
public class KanbanTaskStatusChangedEvent extends ApplicationEvent {

    private final UUID taskId;
    private final String newState;
    private final String userId;

    public KanbanTaskStatusChangedEvent(Object source, UUID taskId, String newState, String userId) {
        super(source);
        this.taskId = taskId;
        this.newState = newState;
        this.userId = userId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getNewState() {
        return newState;
    }

    public String getUserId() {
        return userId;
    }
}
