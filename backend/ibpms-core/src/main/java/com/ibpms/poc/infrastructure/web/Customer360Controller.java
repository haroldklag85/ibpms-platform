package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.Customer360DTO;
import com.ibpms.poc.application.port.in.ObtenerCasosClienteUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint para Vistas 360 del Cliente (Pantalla 17)
 */
@RestController
@RequestMapping("/api/v1/customers")
public class Customer360Controller {

    private final ObtenerCasosClienteUseCase obtenerCasosClienteUseCase;

    public Customer360Controller(ObtenerCasosClienteUseCase obtenerCasosClienteUseCase) {
        this.obtenerCasosClienteUseCase = obtenerCasosClienteUseCase;
    }

    @GetMapping("/{crmId}/cases360")
    @PreAuthorize("hasRole('Admin_Intake') or hasRole('Funcionario')") // Autorización de ejemplo, ajustar si se
                                                                       // requiere ABAC estricto
    public ResponseEntity<Customer360DTO> getCustomer360View(@PathVariable String crmId) {
        Customer360DTO customer360Data = obtenerCasosClienteUseCase.obtenerVista360(crmId);
        return ResponseEntity.ok(customer360Data);
    }

    // GAP: Integration Customer 360 View
    @GetMapping("/{id}/360")
    public ResponseEntity<Map<String, Object>> getCustomer360(@PathVariable("id") String id) {
        // Mock Implementation
        return ResponseEntity.ok(Map.of(
                "customerId", id,
                "fullName", "Mock Customer John Doe",
                "scoreRisk", 45,
                "status", "ACTIVE",
                "openTickets", 2));
    }
}
