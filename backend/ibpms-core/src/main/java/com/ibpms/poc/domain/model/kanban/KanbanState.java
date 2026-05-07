package com.ibpms.poc.domain.model.kanban;

import java.util.EnumSet;
import java.util.Map;

public enum KanbanState {
    TODO, IN_PROGRESS, BLOCKED, DONE;

    private static final Map<KanbanState, EnumSet<KanbanState>> TRANSITIONS = Map.of(
        TODO, EnumSet.of(IN_PROGRESS),
        IN_PROGRESS, EnumSet.of(BLOCKED, DONE),
        BLOCKED, EnumSet.of(IN_PROGRESS),
        DONE, EnumSet.noneOf(KanbanState.class)
    );

    public boolean canTransitionTo(KanbanState target) {
        return TRANSITIONS.getOrDefault(this, EnumSet.noneOf(KanbanState.class)).contains(target);
    }

    public boolean isImmutable() {
        return this == DONE;
    }
}
