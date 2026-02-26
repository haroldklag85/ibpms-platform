package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import org.springframework.stereotype.Service;

@Service
public class DesplegarDefinicionService implements DesplegarDefinicionUseCase {

    private final ProcesoBpmPort procesoBpmPort;

    public DesplegarDefinicionService(ProcesoBpmPort procesoBpmPort) {
        this.procesoBpmPort = procesoBpmPort;
    }

    @Override
    public void desplegarDesdeWeb(DeploymentRequestDTO request) {
        if (request == null || request.getXmlString() == null || request.getXmlString().trim().isEmpty()) {
            throw new IllegalArgumentException("El XML del modelo no puede estar vacío.");
        }

        String resourceName = request.getResourceName();
        if (resourceName == null || resourceName.trim().isEmpty()) {
            resourceName = "proceso_dinamico_" + System.currentTimeMillis() + ".bpmn";
        } else if (!resourceName.endsWith(".bpmn") && !resourceName.endsWith(".dmn")) {
            resourceName += ".bpmn"; // Asegurar extensión válida por defecto
        }

        procesoBpmPort.desplegarProceso(resourceName, request.getXmlString());
    }
}
