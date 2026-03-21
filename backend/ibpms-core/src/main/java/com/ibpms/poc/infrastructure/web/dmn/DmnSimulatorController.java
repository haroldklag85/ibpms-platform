package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.DmnSimulatorUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adapter-In REST para el Simulador DMN (Pantalla 4 / Taller DMN). CA-11
 */
@RestController
@RequestMapping("/api/v1/dmn-models")
public class DmnSimulatorController {

    private final DmnSimulatorUseCase dmnSimulatorUseCase;

    public DmnSimulatorController(DmnSimulatorUseCase dmnSimulatorUseCase) {
        this.dmnSimulatorUseCase = dmnSimulatorUseCase;
    }

    /**
     * Endpoint aséptico de simulación (No guarda nada).
     * @param payload RequestBody que debe contener el "xml" y un map de "variables".
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<Object> simulateDmn(@RequestBody Map<String, Object> payload) {
        
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
