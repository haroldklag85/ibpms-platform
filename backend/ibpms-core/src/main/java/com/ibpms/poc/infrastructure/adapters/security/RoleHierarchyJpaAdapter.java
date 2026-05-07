package com.ibpms.poc.infrastructure.adapters.security;

import com.ibpms.poc.application.ports.out.RoleHierarchyPort;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleHierarchyRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleHierarchyJpaAdapter implements RoleHierarchyPort {

    private static final Logger log = LoggerFactory.getLogger(RoleHierarchyJpaAdapter.class);

    private final RoleHierarchyRepository hierarchyRepository;
    private final RoleTemplateRepository roleTemplateRepository;

    public RoleHierarchyJpaAdapter(RoleHierarchyRepository hierarchyRepository,
                                   RoleTemplateRepository roleTemplateRepository) {
        this.hierarchyRepository = hierarchyRepository;
        this.roleTemplateRepository = roleTemplateRepository;
    }

    @Override
    public void saveHierarchy(UUID parentRoleId, UUID childRoleId) {
        RoleTemplateEntity parent = roleTemplateRepository.findById(parentRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol padre no encontrado: " + parentRoleId));
        RoleTemplateEntity child = roleTemplateRepository.findById(childRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol hijo no encontrado: " + childRoleId));

        RoleHierarchyEntity entity = new RoleHierarchyEntity();
        entity.setParentRole(parent);
        entity.setChildRole(child);

        log.info("US-036: Registrada herencia {} → {}", parent.getRoleName(), child.getRoleName());
        hierarchyRepository.save(entity);
    }

    @Override
    public List<String> resolveInheritedRoleNames(UUID roleTemplateId) {
        List<UUID> ancestorIds = hierarchyRepository.findAllAncestorRoleIds(roleTemplateId);

        if (ancestorIds.isEmpty()) {
            return Collections.emptyList();
        }

        return roleTemplateRepository.findAllById(ancestorIds).stream()
                .map(RoleTemplateEntity::getRoleName)
                .collect(Collectors.toList());
    }

    @Override
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

    @Override
    public boolean existsHierarchy(UUID parentRoleId, UUID childRoleId) {
        return hierarchyRepository.existsByParentRoleIdAndChildRoleId(parentRoleId, childRoleId);
    }

    @Override
    public List<UUID> findAllAncestorRoleIds(UUID roleId) {
        return hierarchyRepository.findAllAncestorRoleIds(roleId);
    }

    @Override
    public Optional<UUID> findRoleIdByName(String roleName) {
        return roleTemplateRepository.findByRoleName(roleName)
                .map(RoleTemplateEntity::getId);
    }
}
