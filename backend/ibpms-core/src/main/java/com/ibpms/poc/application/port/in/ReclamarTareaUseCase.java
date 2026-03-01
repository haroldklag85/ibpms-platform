package com.ibpms.poc.application.port.in;

public interface ReclamarTareaUseCase {
    /**
     * Reclama una tarea para el usuario actual.
     * 
     * @param taskId   ID de la tarea a reclamar.
     * @param username Usuario que reclama la tarea.
     */
    void reclamar(String taskId, String username);
}
