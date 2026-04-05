package com.ibpms.poc.infrastructure.web.admin;

import com.ibpms.poc.application.service.JwtBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "User Identity & Session Admin", description = "Operaciones de administración y revocación de identidades")
@PreAuthorize("hasRole('ADMIN_IT')")
public class SessionRevocationController {

    private static final Logger log = LoggerFactory.getLogger(SessionRevocationController.class);
    private final JwtBlacklistService jwtBlacklistService;

    public SessionRevocationController(JwtBlacklistService jwtBlacklistService) {
        this.jwtBlacklistService = jwtBlacklistService;
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.getName() != null) ? authentication.getName() : "SYSTEM";
    }

    @PostMapping("/{userId}/revoke-session")
    @Operation(summary = "Kill-Session: Revocar sesión activa de un usuario (CA-21 / CA-14)")
    public ResponseEntity<String> revokeUserSession(@PathVariable String userId) {
        String actor = getCurrentUser();
        
        log.warn("SUDO INVOKE [Audit Trail]: Revocación de sesión (Kill-Session) solicitada para userId: {}. Acción ejecutada por usuario administrativo: {}", userId, actor);
        
        // Delegamos a la lógica conectada con Redis (US-038)
        jwtBlacklistService.revokeSession(userId);
        
        return ResponseEntity.ok("Sesión revocada exitosamente e inyectada en la lista negra (Redis).");
    }
}
