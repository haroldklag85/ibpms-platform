package com.ibpms.poc.application.port.out;

public interface CrmFederationPort {

    /**
     * Intenta conectar con el sistema maestro remoto y traer los datos del
     * catálogo.
     * 
     * @param catalogId El ID del catálogo buscado.
     * @return El JSON crudo si el sistema responde dentro del umbral de tiempo.
     */
    String fetchCatalogFromCrm(String catalogId);
}
