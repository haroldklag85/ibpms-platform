package com.ibpms.poc.application.port.in;

public interface AuditLogUseCase {

    /**
     * Recupera el diferencial en JSON de todas las propiedades variables que
     * cambiaron.
     * 
     * @param entityType El nombre de la clase (Ej. "ExpedienteEntity")
     * @param entityId   El UUID o PK de la fila
     * @return El JSON literal con el left (antes) y el right (después) devuelto por
     *         Javers.
     */
    String getEntityDiffHistory(String entityType, String entityId);
}
