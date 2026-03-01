package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.PublicTrackingDTO;
import com.ibpms.poc.application.port.in.RastrearTramitePublicoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint Público B2B/B2C para Trazabilidad Externa (Pantalla 18).
 * Sin pre-authorizations (Totalmente Anónimo), sujeto a rate-limiting o CAPTCHA
 * en API Gateway.
 */
@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final RastrearTramitePublicoUseCase rastrearTramitePublicoUseCase;

    public TrackingController(RastrearTramitePublicoUseCase rastrearTramitePublicoUseCase) {
        this.rastrearTramitePublicoUseCase = rastrearTramitePublicoUseCase;
    }

    @GetMapping("/{trackingCode}")
    public ResponseEntity<PublicTrackingDTO> trackCase(@PathVariable String trackingCode) {
        // La validación interna lanzará un 404 EntityNotFoundException manejado por
        // GlobalExceptionHandler
        PublicTrackingDTO state = rastrearTramitePublicoUseCase.rastrear(trackingCode);
        return ResponseEntity.ok(state);
    }
}
