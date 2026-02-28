package com.ibpms.poc.application.port.out;

import java.util.Optional;

public interface CatalogCachePort {

    /**
     * Guarda o actualiza un JSON crudo asociado a un ID de catálogo.
     * 
     * @param catalogId El ID del catálogo (Ej. CRM_CUSTOMER_SEGMENTS)
     * @param payload   El JSON devuelto originalmente por el sistema remoto
     */
    void updateCache(String catalogId, String payload);

    /**
     * Recupera el backup del catálogo si el sistema maestro ha caído.
     * 
     * @param catalogId El ID del catálogo
     * @return El JSON crudo en caso de existir en caché
     */
    Optional<String> getCatalogFallback(String catalogId);
}
