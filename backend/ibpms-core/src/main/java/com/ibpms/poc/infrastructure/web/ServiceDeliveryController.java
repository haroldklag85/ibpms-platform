package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.ManualStartDTO;
import com.ibpms.poc.application.port.in.IniciarServicioManualUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoint para Instanciación Manual Plan B (Intake Admin / Pantalla 16)
 */
@RestController
@RequestMapping("/api/v1")
public class ServiceDeliveryController {

    private final IniciarServicioManualUseCase iniciarServicioManualUseCase;

    public ServiceDeliveryController(IniciarServicioManualUseCase iniciarServicioManualUseCase) {
        this.iniciarServicioManualUseCase = iniciarServicioManualUseCase;
    }

    @PostMapping("/manual-start")
    @PreAuthorize("hasRole('Admin_Intake')")
    public ResponseEntity<Map<String, String>> manualStart(@RequestBody ManualStartDTO request) {

        UUID newCaseId = iniciarServicioManualUseCase.iniciarServicio(request);

        Map<String, String> response = new HashMap<>();
        response.put("caseId", newCaseId.toString());
        response.put("status", "STARTED");
        response.put("message", "Expediente manual iniciado correctamente.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GAP: Integration Kanban Update
    @PatchMapping("/kanban/items/{id}/status")
    public ResponseEntity<Void> updateKanbanStatus(@PathVariable("id") String id,
            @RequestBody Map<String, Object> request) {
        // Mock Implementation: Just acknowledge the update
        return ResponseEntity.noContent().build();
    }
}
