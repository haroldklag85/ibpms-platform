package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleHierarchyRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    private final RoleHierarchyRepository hierarchyRepository;
    private final RoleTemplateRepository roleTemplateRepository;

    public RoleHierarchyService(RoleHierarchyRepository hierarchyRepository,
                                RoleTemplateRepository roleTemplateRepository) {
        this.hierarchyRepository = hierarchyRepository;
        this.roleTemplateRepository = roleTemplateRepository;
    }

    /**
     * Registra una relación jerárquica padre→hijo.
     * OBLIGATORIAMENTE valida la ausencia de ciclos antes de persistir.
     *
     * @throws IllegalStateException si la inserción generaría un ciclo.
     */
    @Transactional
    public RoleHierarchyEntity registerHierarchy(UUID parentRoleId, UUID childRoleId) {
        // Guardia 1: No se permite auto-referencia directa
        if (parentRoleId.equals(childRoleId)) {
            throw new IllegalStateException(
                    "Ciclo detectado: Un rol no puede ser padre de sí mismo. RoleId=" + parentRoleId);
        }

        // Guardia 2: Verificar que la relación inversa no existe (child → parent)
        if (hierarchyRepository.existsByParentRoleIdAndChildRoleId(childRoleId, parentRoleId)) {
            throw new IllegalStateException(
                    "Ciclo detectado: Ya existe la relación inversa child=" + childRoleId 
                    + " → parent=" + parentRoleId + ". Insertar generaría un bucle.");
        }

        // Guardia 3: Verificar que el child propuesto no sea ya ancestro del parent (ciclo indirecto)
        List<UUID> ancestorsOfParent = hierarchyRepository.findAllAncestorRoleIds(parentRoleId);
        if (ancestorsOfParent.contains(childRoleId)) {
            throw new IllegalStateException(
                    "Ciclo indirecto detectado: El rol hijo (" + childRoleId 
                    + ") ya es ancestro del rol padre (" + parentRoleId 
                    + "). La cadena resultaría en un bucle infinito.");
        }

        // Guardia 4: No duplicar la misma relación
        if (hierarchyRepository.existsByParentRoleIdAndChildRoleId(parentRoleId, childRoleId)) {
            log.warn("Relación jerárquica ya existe: parent={} → child={}. Ignorando duplicado.", 
                     parentRoleId, childRoleId);
            return hierarchyRepository.findByParentRoleId(parentRoleId).stream()
                    .filter(h -> h.getChildRole().getId().equals(childRoleId))
                    .findFirst()
                    .orElseThrow();
        }

        RoleTemplateEntity parent = roleTemplateRepository.findById(parentRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol padre no encontrado: " + parentRoleId));
        RoleTemplateEntity child = roleTemplateRepository.findById(childRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol hijo no encontrado: " + childRoleId));

        RoleHierarchyEntity entity = new RoleHierarchyEntity();
        entity.setParentRole(parent);
        entity.setChildRole(child);

        log.info("US-036: Registrada herencia {} → {}", parent.getRoleName(), child.getRoleName());
        return hierarchyRepository.save(entity);
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
        List<UUID> ancestorIds = hierarchyRepository.findAllAncestorRoleIds(roleTemplateId);

        if (ancestorIds.isEmpty()) {
            return Collections.emptyList();
        }

        return roleTemplateRepository.findAllById(ancestorIds).stream()
                .map(RoleTemplateEntity::getRoleName)
                .collect(Collectors.toList());
    }

    /**
     * Dado un conjunto de role names directos del usuario, resuelve y acumula
     * todos los roles heredados de la pirámide jerárquica.
     * Retorna la unión de roles directos + roles heredados (sin duplicados).
     */
    @Transactional(readOnly = true)
    public Set<String> resolveAllEffectiveRoles(Set<String> directRoleNames) {
        Set<String> effectiveRoles = new LinkedHashSet<>(directRoleNames);

        for (String roleName : directRoleNames) {
            roleTemplateRepository.findByRoleName(roleName).ifPresent(template -> {
                List<String> inherited = resolveInheritedRoleNames(template.getId());
                effectiveRoles.addAll(inherited);
            });
        }

        return effectiveRoles;
    }
}
