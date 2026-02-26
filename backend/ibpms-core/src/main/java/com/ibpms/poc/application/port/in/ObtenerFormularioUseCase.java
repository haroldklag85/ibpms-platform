package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.FormSchemaDTO;

public interface ObtenerFormularioUseCase {
    /**
     * Retorna el esquema JSON (Server-Driven UI) para renderizar dinámicamente
     * el formulario asociado a una User Task específica.
     */
    FormSchemaDTO obtenerFormulario(String taskId);
}
