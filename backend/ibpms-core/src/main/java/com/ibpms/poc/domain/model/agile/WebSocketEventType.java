package com.ibpms.poc.domain.model.agile;

/**
 * Vocabulario Estandarizado STOMP/WebSocket para sincronización en tiempo real.
 * CA-27 (Sprint 5 Iter 4).
 */
public enum WebSocketEventType {
    /** Una tarea individual fue reclamada exitosamente. */
    TASK_CLAIMED,
    
    /** Una tarea individual fue liberada. */
    TASK_UNCLAIMED,
    
    /** Despojo autoritario por reasignación forzada o caducidad. */
    TASK_FORCE_UNCLAIMED,
    
    /** Solicitud masiva (bulk claim/unclaim) finalizada exitosamente para agrupar renders. */
    TASKS_BULK_UPDATED,
    
    /** Tarea completada positivamente, debe removerse del cliente. */
    TASK_COMPLETED,
    
    /** El SLA interno llegó a cero, puede generar reasignación en cola. */
    TASK_EXPIRED,
    
    /** Notificación global a los operadores en pool para traer nueva fila de la DB (Post-Claim de otros). */
    TASK_POOL_REFRESH
}
