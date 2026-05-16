package com.ibpms.poc.application.ports.out;

import java.util.List;

/**
 * Outbound Port para obtener la configuración del formulario genérico a nivel de proceso.
 * Desacopla la lógica de negocio del repositorio JPA BpmnProcessDesignRepository.
 * Requerimiento: US-039 (Formulario Genérico Dinámico)
 * Soportando los Criterios de Aceptación para recuperación de Whitelist (CA-5).
 */
public interface GenericProcessDefinitionPort {
    List<String> getGenericFormWhitelist(String processKey);
}
