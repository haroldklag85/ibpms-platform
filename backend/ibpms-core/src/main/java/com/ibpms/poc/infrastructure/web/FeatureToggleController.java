package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @Traceability(US = "US-001", CA = {"CA-08"})
 * POR QUÉ: Controlador REST de Toggles ajustado para cumplir con la Arquitectura Hexagonal.
 * Se inyecta la interfaz de Caso de Uso (UpdateFeatureToggleUseCase) en lugar del repositorio JPA,
 * evitando el acoplamiento a infraestructura de base de datos directa.
 */
@RestController
@RequestMapping("/api/v1/workdesk/feature-toggles")
@Traceability(US = "US-001", CA = {"CA-08"})
@Tag(name = "Feature Toggles", description = "Gestión administrativa de Feature Toggles")
public class FeatureToggleController {

    private final com.ibpms.poc.application.ports.in.UpdateFeatureToggleUseCase updateFeatureToggleUseCase;

    public FeatureToggleController(com.ibpms.poc.application.ports.in.UpdateFeatureToggleUseCase updateFeatureToggleUseCase) {
        this.updateFeatureToggleUseCase = updateFeatureToggleUseCase;
    }

    private String resolveTenantId(Authentication authentication) {
        try {
            return com.ibpms.poc.application.util.SecurityContextUtils.getTenantId();
        } catch (Exception e) {
            return (authentication != null && authentication.getName() != null) ? authentication.getName() : "default";
        }
    }

    @GetMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar Feature Toggle", description = "Devuelve el estado (enabled/disabled) de un feature toggle.")
    public ResponseEntity<?> getFeatureToggle(@PathVariable String key, Authentication authentication) {
        String tenantId = resolveTenantId(authentication);

        boolean enabled = updateFeatureToggleUseCase.isFeatureEnabled(tenantId, key);
        
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Actualizar Feature Toggle", description = "Permite a un SUPER_ADMIN activar/desactivar features globalmente.")
    @Traceability(US = "US-001", CA = {"CA-08", "CA-16"})
    public ResponseEntity<?> updateFeatureToggle(@PathVariable String key, @org.springframework.web.bind.annotation.RequestBody Map<String, Boolean> body, Authentication authentication) {
        if (body == null || !body.containsKey("enabled")) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Body must contain 'enabled' key");
        }
        
        String tenantId = resolveTenantId(authentication);
        
        Boolean reqEnabled = body.get("enabled");
        boolean enabled = updateFeatureToggleUseCase.updateFeatureToggle(tenantId, key, reqEnabled);
        
        return ResponseEntity.ok(Map.of("key", key, "enabled", enabled, "tenantId", tenantId));
    }
}
