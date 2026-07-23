package com.ibpms.poc.application.service;

import org.springframework.stereotype.Component;
import java.util.EnumSet;
import java.util.Map;

@Component
public class KanbanStateMachine {

    public enum KanbanState {
        TODO, IN_PROGRESS, BLOCKED, DONE
    }

    /**
     * Transiciones válidas — alineadas con handoff_s6_2_backend.md §B5.
     * DONE es INMUTABLE (EnumSet vacío) — CU-J04-NEG-06.
     * TODO solo puede avanzar a IN_PROGRESS (no saltar a BLOCKED/DONE).
     */
    private static final Map<KanbanState, EnumSet<KanbanState>> TRANSITIONS = Map.of(
        KanbanState.TODO, EnumSet.of(KanbanState.IN_PROGRESS),
        KanbanState.IN_PROGRESS, EnumSet.of(KanbanState.BLOCKED, KanbanState.DONE),
        KanbanState.BLOCKED, EnumSet.of(KanbanState.IN_PROGRESS),
        KanbanState.DONE, EnumSet.noneOf(KanbanState.class) // INMUTABLE
    );

    public void validateTransition(String currentStateStr, String newStateStr) {
        KanbanState currentState;
        KanbanState newState;

        try {
            currentState = KanbanState.valueOf(currentStateStr);
            newState = KanbanState.valueOf(newStateStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Estado Kanban inválido proveído.");
        }

        if (!TRANSITIONS.getOrDefault(currentState, EnumSet.noneOf(KanbanState.class)).contains(newState)) {
            throw new IllegalStateException("Transición de estado Kanban no permitida: " + currentState + " -> " + newState);
        }
    }
}
