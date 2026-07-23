package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.JwtBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador administrativo para operaciones de seguridad críticas (CA-14).
 * Permite la revocación inmediata de sesiones (Kill-Session).
 */
@RestController
@RequestMapping("/api/v1/admin/security")
public class SecurityAdminController {

    private static final Logger log = LoggerFactory.getLogger(SecurityAdminController.class);
    private final JwtBlacklistService blacklistService;

    public SecurityAdminController(JwtBlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping("/delegations")
    public org.springframework.http.ResponseEntity<java.util.List<Object>> getDelegations() {
        return org.springframework.http.ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PostMapping("/delegations")
    public org.springframework.http.ResponseEntity<Object> createDelegation(@RequestBody Object payload) {
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(payload);
    }

    @DeleteMapping("/delegations/{id}")
    public org.springframework.http.ResponseEntity<Void> revokeDelegation(@PathVariable java.util.UUID id) {
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    /**
     * CA-14: Revocación de sesión (Kill-Session).
     * Invalida el token del usuario en tiempo real mediante Redis.
     */
    @PostMapping("/users/{userId}/revoke-session")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> revokeSession(@PathVariable String userId) {
        log.info("CA-14: Solicitud de Kill-Session para usuario [{}] recibida.", userId);
        
        blacklistService.revokeSession(userId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Sesión revocada exitosamente",
                "userId", userId,
                "status", "REVOKED"
        ));
    }
}
