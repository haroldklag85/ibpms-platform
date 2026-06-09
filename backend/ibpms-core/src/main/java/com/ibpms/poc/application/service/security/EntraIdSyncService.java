// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to handle synchronization with Microsoft EntraID (SSO).
 * Part of CA-01 US-036: Dual Motor Identity Mapping.
 */
@Service
@RequiredArgsConstructor
public class EntraIdSyncService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Fetches available groups from EntraID.
     * In V1, this returns a realistic set of groups based on naming conventions.
     */
    public List<Map<String, String>> fetchAvailableGroups() {
        // En V1, esto simula el fetch de grupos con prefijo GG_IBPMS
        return List.of(
            Map.of("id", "1111-2222", "displayName", "GG_IBPMS_Admins"),
            Map.of("id", "3333-4444", "displayName", "GG_IBPMS_Viewers")
        );
    }

    /**
     * CA-08 US-036: JIT Provisioning (Aprovisionamiento Silencioso SSO).
     * Crea el usuario localmente si no existe tras una autenticación SSO exitosa.
     */
    @Transactional
    public UserEntity provisionUser(String username, Map<String, String> claims) {
        List<String> missingFields = new java.util.ArrayList<>();
        if (!claims.containsKey("email") || claims.get("email") == null || claims.get("email").isBlank()) missingFields.add("email");
        if (!claims.containsKey("name") || claims.get("name") == null || claims.get("name").isBlank()) missingFields.add("name");
        if (!claims.containsKey("Sucursal_ID") || claims.get("Sucursal_ID") == null || claims.get("Sucursal_ID").isBlank()) missingFields.add("Sucursal_ID");
        if (!claims.containsKey("Codigo_Jefe") || claims.get("Codigo_Jefe") == null || claims.get("Codigo_Jefe").isBlank()) missingFields.add("Codigo_Jefe");

        if (!missingFields.isEmpty()) {
            throw new com.ibpms.poc.application.service.security.exceptions.PreconditionRequiredException("Claims obligatorios faltantes para JIT Provisioning.", missingFields);
        }

        String email = claims.get("email");
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setIsExternalIdp(true);
        newUser.setStatus(UserStatus.ACTIVE);
        
        try {
            newUser.setJitClaimsJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(claims));
        } catch (Exception e) {
            newUser.setJitClaimsJson("{}");
        }

        // Asignación de Rol Base por defecto (Ciudadano Interno)
        RoleEntity baseRole = roleRepository.findByName("ROLE_USER_INTERNAL")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity("ROLE_USER_INTERNAL", "Aprovisionamiento JIT Automático");
                    return roleRepository.save(role);
                });
        
        newUser.getRoles().add(baseRole);
        return userRepository.save(newUser);
    }
}
