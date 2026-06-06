package com.ibpms.poc.domain.model.kanban;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KanbanTaskTest {

    @Test
    void testIsImmutableWhenDone() {
        KanbanTask task = new KanbanTask();
        task.setStatus(KanbanState.DONE);
        assertTrue(task.isImmutable());
    }

    @Test
    void testValidateTransitionInvalid() {
        KanbanTask task = new KanbanTask();
        task.setStatus(KanbanState.TODO);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> task.validateTransition(KanbanState.DONE));
        assertTrue(exception.getMessage().contains("Transición inválida"));
    }

    @Test
    void testValidateTransitionWhenImmutable() {
        KanbanTask task = new KanbanTask();
        task.setStatus(KanbanState.DONE);
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> task.validateTransition(KanbanState.TODO));
        assertEquals("Tarea en DONE es inmutable", exception.getMessage());
    }

    @Test
    void testRequireBlockedReasonEmpty() {
        KanbanTask task = new KanbanTask();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> task.requireBlockedReason("  "));
        assertEquals("Se requiere una razón para bloquear la tarea", exception.getMessage());
    }

    @Test
    void testRequireBlockedReasonValid() {
        KanbanTask task = new KanbanTask();
        task.requireBlockedReason("Falta de insumos");
        assertEquals("Falta de insumos", task.getBlockedReason());
    }
}
