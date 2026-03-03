package com.ibpms.poc.application.port.in;

import java.util.Map;

public interface LiberarTareaUseCase {
    /**
     * Libera una tarea que el usuario actualmente tiene asignada, devolviéndola al
     * pool (Draft).
     * 
     * @param taskId         El ID de la tarea
     * @param username       El usuario que la está liberando
     * @param partialPayload Las variables del formulario parciales (borrador)
     * @param reason         El motivo opcional de por qué no la finalizó
     */
    void liberar(String taskId, String username, Map<String, Object> partialPayload, String reason);
}
