package com.ibpms.poc.application.ports.out;

import java.util.Optional;

/**
 * Puerto de salida para acceder a la definición y esquema de los formularios.
 * @Traceability: US-029 - OBS-QA-01 - Desacoplar la validación Zod/JSON y obtener el schema real
 */
public interface FormDefinitionPort {
    Optional<String> findSchemaContentByVersion(String schemaVersion);
}
