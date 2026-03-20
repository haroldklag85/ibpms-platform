package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.security.SecurityAnomalyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * CA-13: Tablero CISO de Anomalías. Blindado con Roles de Ciberseguridad.
 */
@RestController
@RequestMapping("/api/v1/security/anomalies")
public class SecurityAnomalyController {

    private final SecurityAnomalyService anomalyService;

    public SecurityAnomalyController(SecurityAnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    /**
     * CA-12: Listado de cacería. Exclusivo para Oficiales y Súper Admins.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CISO', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAnomalies(@RequestParam(defaultValue = "OPEN") String status) {
        return ResponseEntity.ok(anomalyService.getAnomaliesByStatus(status));
    }

    /**
     * CA-12: Resolución indeleble de Incidentes Críticos.
     */
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_CISO', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> resolveAnomaly(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cisoId = auth.getName(); // Trae el Subject JWT
        
        return ResponseEntity.ok(anomalyService.resolveAnomaly(id, cisoId));
    }
}
