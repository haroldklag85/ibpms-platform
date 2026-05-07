package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.security.RoleHierarchyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BFF Controller para Auth y Security.
 * Sirve datos orientados a UI en modelo Zero-Trust.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Traceability(US = "US-025", CA = {"CA-09"})
public class AuthBffController {

    private final RoleHierarchyService roleHierarchyService;

    public AuthBffController(RoleHierarchyService roleHierarchyService) {
        this.roleHierarchyService = roleHierarchyService;
    }

    /**
     * US-025 CA-09: Retorna lista plana de roles efectivos asociados a la sesión.
     * Estos roles ya pasaron por el filtro JwtAuthFilter que invocó a RoleHierarchyService (CTE),
     * por ende están cacheados en el Authentication object.
     */
    @GetMapping("/effective-roles")
    public ResponseEntity<List<String>> getEffectiveRoles(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<String> effectiveRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toList());

        return ResponseEntity.ok(effectiveRoles);
    }
}
