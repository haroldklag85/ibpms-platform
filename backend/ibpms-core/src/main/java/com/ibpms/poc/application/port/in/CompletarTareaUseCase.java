package com.ibpms.poc.application.port.in;

import java.util.Map;

public interface CompletarTareaUseCase {
    /**
     * Completa una tarea en el motor BPM aportando el payload del formulario.
     */
    void completar(String taskId, Map<String, Object> variables, String idempotencyKey);
}
