package com.ibpms.poc.domain.model.kanban;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KanbanStateTest {

    @Test
    void testValidTransitions() {
        assertTrue(KanbanState.TODO.canTransitionTo(KanbanState.IN_PROGRESS));
        assertTrue(KanbanState.IN_PROGRESS.canTransitionTo(KanbanState.BLOCKED));
        assertTrue(KanbanState.IN_PROGRESS.canTransitionTo(KanbanState.DONE));
        assertTrue(KanbanState.BLOCKED.canTransitionTo(KanbanState.IN_PROGRESS));
    }

    @Test
    void testInvalidTransitions() {
        assertFalse(KanbanState.TODO.canTransitionTo(KanbanState.BLOCKED));
        assertFalse(KanbanState.TODO.canTransitionTo(KanbanState.DONE));
        assertFalse(KanbanState.BLOCKED.canTransitionTo(KanbanState.DONE));
        assertFalse(KanbanState.DONE.canTransitionTo(KanbanState.TODO));
        assertFalse(KanbanState.DONE.canTransitionTo(KanbanState.IN_PROGRESS));
        assertFalse(KanbanState.DONE.canTransitionTo(KanbanState.BLOCKED));
    }

    @Test
    void testIsImmutable() {
        assertFalse(KanbanState.TODO.isImmutable());
        assertFalse(KanbanState.IN_PROGRESS.isImmutable());
        assertFalse(KanbanState.BLOCKED.isImmutable());
        assertTrue(KanbanState.DONE.isImmutable());
    }
}
