package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.usecase.security.ImpersonationUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;

import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/impersonate")
public class ImpersonationController {

    private final ImpersonationUseCase impersonationUseCase;
    private final UserRepository userRepository;

    public ImpersonationController(ImpersonationUseCase impersonationUseCase, UserRepository userRepository) {
        this.impersonationUseCase = impersonationUseCase;
        this.userRepository = userRepository;
    }

    @PostMapping("/{targetUserId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> startImpersonation(@PathVariable UUID targetUserId, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> adminOpt = userRepository.findByUsername(auth.getName());
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        UUID adminId = adminOpt.get().getId();
        
        try {
            String token = impersonationUseCase.startImpersonation(adminId, targetUserId, request);
            return ResponseEntity.ok(Map.of("token", token, "expiresIn", 1800));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/exit")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
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

        Optional<UserEntity> targetOpt = userRepository.findByUsername(auth.getName());
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        UUID targetUserId = targetOpt.get().getId();

        String token = impersonationUseCase.exitImpersonation(adminId, targetUserId, request);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
