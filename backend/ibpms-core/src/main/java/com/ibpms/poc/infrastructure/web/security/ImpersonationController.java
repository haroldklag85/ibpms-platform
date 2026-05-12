package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.usecase.security.ImpersonationUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/impersonate")
public class ImpersonationController {

    private final ImpersonationUseCase impersonationUseCase;

    public ImpersonationController(ImpersonationUseCase impersonationUseCase) {
        this.impersonationUseCase = impersonationUseCase;
    }

    @PostMapping("/{targetUserId}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> startImpersonation(@PathVariable UUID targetUserId, HttpServletRequest request) {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        try {
            String token = impersonationUseCase.startImpersonation(adminUsername, targetUserId, request);
            return ResponseEntity.ok(Map.of("token", token, "expiresIn", 1800));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> exitImpersonation(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Extraer impersonatedBy desde los detalles del contexto
        UUID adminId = null;
        if (auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> details = (Map<String, String>) auth.getDetails();
            String impersonatedBy = details.get("impersonatedBy");
            if (impersonatedBy != null) {
                adminId = UUID.fromString(impersonatedBy);
            }
        }

        if (adminId == null) {
            // Si no estaba impersonando, es un error
            return ResponseEntity.status(400).body(Map.of("error", "No está en una sesión de impersonación"));
        }

        String targetUsername = auth.getName();

        try {
            // Logout Zombie mitigado: Se destruye la sesión forzando un re-login.
            impersonationUseCase.exitImpersonation(adminId, targetUsername, request);
            return ResponseEntity.ok(Map.of("message", "Impersonation exited. Session invalidated. Please login again."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
