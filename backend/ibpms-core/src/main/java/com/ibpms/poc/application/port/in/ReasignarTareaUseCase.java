package com.ibpms.poc.application.port.in;

public interface ReasignarTareaUseCase {
    /**
     * Reasigna directamente una tarea entre recursos humanos evitando reingreso al
     * pool.
     * Supervisa los rebotes máximos.
     * 
     * @param taskId          El ID de la tarea
     * @param currentUsername El usuario emisor actual
     * @param newUserId       El usuario target
     * @param reason          El motivo
     */
    void reasignar(String taskId, String currentUsername, String newUserId, String reason);
}
