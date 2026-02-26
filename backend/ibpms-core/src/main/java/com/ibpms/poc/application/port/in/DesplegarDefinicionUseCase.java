package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.DeploymentRequestDTO;

public interface DesplegarDefinicionUseCase {
    /**
     * Inyecta dinámicamente un modelo de procesos BPMN o DMN en el motor
     * a partir del payload enviado desde la interfaz web.
     * 
     * @param request Datos del despliegue conteniendo el XML y el nombre
     */
    void desplegarDesdeWeb(DeploymentRequestDTO request);
}
