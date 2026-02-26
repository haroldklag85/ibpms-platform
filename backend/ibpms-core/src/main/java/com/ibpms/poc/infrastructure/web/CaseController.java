package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.in.CreateExpedienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador Driving — Controlador REST para Expedientes.
 * Expone POST /expedientes (contrato OpenAPI + handoff Lead Architect).
 * Cabecera Idempotency-Key: opcional, gestionada en el servicio.
 */
@RestController
@RequestMapping("/expedientes")
public class CaseController {

    private final CreateExpedienteUseCase createExpedienteUseCase;

    public CaseController(CreateExpedienteUseCase createExpedienteUseCase) {
        this.createExpedienteUseCase = createExpedienteUseCase;
    }

    /**
     * POST /expedientes
     * Crea un nuevo expediente. Devuelve 201 Created con el cuerpo del expediente.
     *
     * @param idempotencyKey Cabecera opcional de idempotencia (Idempotency-Key)
     * @param request        Cuerpo de la petición validado
     */
    @PostMapping
    public ResponseEntity<ExpedienteDTO> crearExpediente(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ExpedienteDTO request) {

        // Inyectar la clave de idempotencia en el DTO para que el servicio la procese
        request.setIdempotencyKey(idempotencyKey);
        ExpedienteDTO created = createExpedienteUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
