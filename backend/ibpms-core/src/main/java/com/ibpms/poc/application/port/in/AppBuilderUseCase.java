package com.ibpms.poc.application.port.in;

import com.ibpms.poc.domain.model.TemplateType;
import com.ibpms.poc.domain.model.UiTemplate;

import java.util.List;

public interface AppBuilderUseCase {

    /**
     * Guarda una nueva plantilla o una nueva versión si ya existe.
     * 
     * @param name    Nombre único de la plantilla.
     * @param type    Tipo de plantilla (VUE_COMPONENT, ZOD_SCHEMA, etc).
     * @param rawCode Código fuente de la plantilla.
     * @return El objeto de dominio UiTemplate con la versión auto-calculada.
     */
    UiTemplate saveTemplate(String name, TemplateType type, String rawCode);

    /**
     * Obtiene la versión más reciente de la plantilla.
     * 
     * @param name Nombre único de la plantilla.
     * @return El objeto de dominio UiTemplate correspondiente.
     */
    UiTemplate getLatestTemplateByName(String name);

    /**
     * Obtiene el historial completo de versiones de una plantilla.
     * 
     * @param name Nombre único de la plantilla.
     * @return Lista de historial.
     */
    List<UiTemplate> getVersionHistory(String name);
}
