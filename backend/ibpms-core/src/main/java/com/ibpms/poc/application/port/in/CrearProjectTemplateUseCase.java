package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.ProjectTemplateDTO;

public interface CrearProjectTemplateUseCase {

    /**
     * Crea un nuevo Project Template (WBS) en base de datos.
     * Solo usuarios con rol "Architect" pueden ejecutar esta acción.
     * 
     * @param dto       El payload del template JSON recibido de Vue
     * @param createdBy Usuario extráido del token JWT
     * @return El DTO con UUID generado y guardado
     */
    ProjectTemplateDTO crearPlantilla(ProjectTemplateDTO dto, String createdBy);
}
