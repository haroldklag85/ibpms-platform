package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryHierarchyTest {

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
