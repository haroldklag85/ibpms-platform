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

    private final RoleHierarchyPort roleHierarchyPort;

    public RoleHierarchyService(RoleHierarchyPort roleHierarchyPort) {
        this.roleHierarchyPort = roleHierarchyPort;
    }

    /**
     * Registra una relación jerárquica padre→hijo.
     * OBLIGATORIAMENTE valida la ausencia de ciclos antes de persistir.
     *
     * @throws IllegalStateException si la inserción generaría un ciclo.
     */
    @Transactional
    public void registerHierarchy(UUID parentRoleId, UUID childRoleId) {
        // Guardia 1: No se permite auto-referencia directa
        if (parentRoleId.equals(childRoleId)) {
            throw new IllegalStateException(
                    "Ciclo detectado: Un rol no puede ser padre de sí mismo. RoleId=" + parentRoleId);
        }

        // Guardia 2: Verificar que la relación inversa no existe (child → parent)
        if (roleHierarchyPort.existsHierarchy(childRoleId, parentRoleId)) {
            throw new IllegalStateException(
                    "Ciclo detectado: Ya existe la relación inversa child=" + childRoleId 
                    + " → parent=" + parentRoleId + ". Insertar generaría un bucle.");
        }

        // Guardia 3: Verificar que el child propuesto no sea ya ancestro del parent (ciclo indirecto)
        List<UUID> ancestorsOfParent = roleHierarchyPort.findAllAncestorRoleIds(parentRoleId);
        if (ancestorsOfParent.contains(childRoleId)) {
            throw new IllegalStateException(
                    "Ciclo indirecto detectado: El rol hijo (" + childRoleId 
                    + ") ya es ancestro del rol padre (" + parentRoleId 
                    + "). La cadena resultaría en un bucle infinito.");
        }

        // Guardia 4: No duplicar la misma relación
        if (roleHierarchyPort.existsHierarchy(parentRoleId, childRoleId)) {
            log.warn("Relación jerárquica ya existe: parent={} → child={}. Ignorando duplicado.", 
                     parentRoleId, childRoleId);
            return;
        }

        roleHierarchyPort.saveHierarchy(parentRoleId, childRoleId);
    }

    /**
     * Resuelve todos los roles heredados (ancestros) para un roleTemplateId dado.
     * Devuelve la lista plana de nombres de rol (Strings) listos para inyección
     * en GrantedAuthority del SecurityContextHolder.
     *
     * @param roleTemplateId El UUID del rol plantilla del que se desea obtener herencias.
     * @return Lista de nombres de roles ancestrales (puede estar vacía si es raíz).
     */
    @Transactional(readOnly = true)
    public List<String> resolveInheritedRoleNames(UUID roleTemplateId) {
        return roleHierarchyPort.resolveInheritedRoleNames(roleTemplateId);
    }

    /**
     * Dado un conjunto de role names directos del usuario, resuelve y acumula
     * todos los roles heredados de la pirámide jerárquica.
     * Retorna la unión de roles directos + roles heredados (sin duplicados).
     */
    @Transactional(readOnly = true)
    public Set<String> resolveAllEffectiveRoles(Set<String> directRoleNames) {
        return roleHierarchyPort.resolveAllEffectiveRoles(directRoleNames);
    }
}
