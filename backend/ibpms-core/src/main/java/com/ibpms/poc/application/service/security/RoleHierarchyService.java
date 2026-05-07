package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.ports.out.RoleHierarchyPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * US-036 CA-6: Servicio de Herencia Piramidal de Roles.
 * 
 * Responsabilidades:
 * 1. Registrar relaciones padre→hijo validando la ausencia de ciclos.
 * 2. Resolver la jerarquía completa de un rol vía CTE recursivo.
 * 3. Exponer los nombres de roles heredados para inyección en GrantedAuthority.
 */
@Service
public class RoleHierarchyService {

    private static final Logger log = LoggerFactory.getLogger(RoleHierarchyService.class);

    // @Traceability: US-036 - CA-06 Herencia de Roles Piramidal
    private final com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository;

    public RoleHierarchyService(com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Dado un conjunto de role names directos del usuario (sin prefijo ROLE_ o con él), resuelve y acumula
     * todos los roles heredados de la pirámide jerárquica.
     * Retorna la unión de roles directos + roles heredados (sin duplicados, sin prefijo ROLE_).
     */
    @Transactional(readOnly = true)
    public Set<String> resolveAllEffectiveRoles(Set<String> directRoleNames) {
        Set<String> effectiveRoles = new LinkedHashSet<>(directRoleNames);

        for (String roleName : directRoleNames) {
            String searchName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            List<String> inherited = roleRepository.findInheritedRoleNamesByName(searchName);
            for (String inheritedName : inherited) {
                effectiveRoles.add(inheritedName.replace("ROLE_", ""));
            }
        }

        return effectiveRoles;
    }
}
