package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.DmnSimulatorUseCase;
import com.ibpms.poc.application.service.security.DmnSimulatorRateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.Map;

/**
 * Adapter-In REST para el Simulador DMN (Pantalla 4 / Taller DMN). CA-11
 */
@RestController
@RequestMapping("/api/v1/dmn-models")
@Tag(name = "DMN Simulator", description = "Simulador aséptico de tablas de decisión DMN en memoria (Zero-Persistence)")
public class DmnSimulatorController {

    private final DmnSimulatorUseCase dmnSimulatorUseCase;
    private final DmnSimulatorRateLimiterService rateLimiterService;

    public DmnSimulatorController(DmnSimulatorUseCase dmnSimulatorUseCase, DmnSimulatorRateLimiterService rateLimiterService) {
        this.dmnSimulatorUseCase = dmnSimulatorUseCase;
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * Endpoint aséptico de simulación (No guarda nada).
     * @param payload RequestBody que debe contener el "xml" y un map de "variables".
     */
    @Operation(summary = "Simula DMN", description = "Ejecuta un DMN XML temporal contra un diccionario de variables sin afectar el motor real.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Simulación exitosa"),
                   @ApiResponse(responseCode = "429", description = "Rate limit excedido (Max 20/min)")
               })
    @PostMapping("/simulate-sandbox")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<Object> simulateDmn(@RequestBody Map<String, Object> payload, Principal principal) {
        
        String userId = (principal != null) ? principal.getName() : "anonymous";
        
        // GAP-20: Rate Limiter para Simulator
        if (!rateLimiterService.tryConsumeToken(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("status", "ERROR", "message", "Rate Limit Excedido. Máximo 20 simulaciones por minuto."));
        }

        String xmlRaw = (String) payload.get("xml");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) payload.get("variables");

        if (xmlRaw == null || variables == null) {
            return ResponseEntity.badRequest().body("Payload Incompleto. Requiere 'xml' y 'variables'.");
        }

        Object result = dmnSimulatorUseCase.simulateDmnEvaluation(xmlRaw, variables);
        
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "simulationResult", result
        ));
    }
}
