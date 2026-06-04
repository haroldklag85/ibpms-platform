// @Traceability: US-005, CA-41 - ADR-001
package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.transaction.annotation.Transactional
public class RoleRepositoryHierarchyTest extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindRoleIdsInTree_PyramidalInheritance() {
        // Setup Hierarchy: A -> B -> C
        RoleEntity roleA = new RoleEntity("ROLE_A", "Gran Parent");
        roleRepository.save(roleA);

        RoleEntity roleB = new RoleEntity("ROLE_B", "Parent");
        roleB.setParentRole(roleA);
        roleRepository.save(roleB);

        RoleEntity roleC = new RoleEntity("ROLE_C", "Child");
        roleC.setParentRole(roleB);
        roleRepository.save(roleC);

        // Execute: Get tree for Role C
        List<UUID> roleIds = roleRepository.findRoleIdsInTree(roleC.getId());

        // Verify: Should contain C, B, A
        assertThat(roleIds).hasSize(3);
        assertThat(roleIds).contains(roleA.getId(), roleB.getId(), roleC.getId());
    }
}
