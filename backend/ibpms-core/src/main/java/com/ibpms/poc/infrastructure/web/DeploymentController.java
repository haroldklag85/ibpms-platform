package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador Driving: Controlador para el Web Modeler del Frontend.
 * Permite la inyección dinámica de BPMNs dibujados por el usuario.
 */
@RestController
@RequestMapping("/api/v1/deployments")
public class DeploymentController {

    private final DesplegarDefinicionUseCase desplegarDefinicionUseCase;

    public DeploymentController(DesplegarDefinicionUseCase desplegarDefinicionUseCase) {
        this.desplegarDefinicionUseCase = desplegarDefinicionUseCase;
    }

    /**
     * Endpoint API Rest - POST /api/v1/deployments
     * Se invoca desde el cliente con un Payload conteniendo el esquema XML/BPMN.
     */
    @PostMapping
    public ResponseEntity<Void> deployProcess(@RequestBody DeploymentRequestDTO request) {
        desplegarDefinicionUseCase.desplegarDesdeWeb(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
