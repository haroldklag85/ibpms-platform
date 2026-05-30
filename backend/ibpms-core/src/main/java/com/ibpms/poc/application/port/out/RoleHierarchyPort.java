// @Traceability: US-003 - ADR-001
package com.ibpms.poc.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleHierarchyPort {
    void saveHierarchy(UUID parentRoleId, UUID childRoleId);
    List<String> resolveInheritedRoleNames(UUID roleTemplateId);
    Set<String> resolveAllEffectiveRoles(Set<String> directRoleNames);
    boolean existsHierarchy(UUID parentRoleId, UUID childRoleId);
    List<UUID> findAllAncestorRoleIds(UUID roleId);
    Optional<UUID> findRoleIdByName(String roleName);
}
