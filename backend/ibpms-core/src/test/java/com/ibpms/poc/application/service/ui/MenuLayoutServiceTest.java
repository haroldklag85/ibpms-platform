package com.ibpms.poc.application.service.ui;


import com.ibpms.poc.infrastructure.jpa.entity.security.PermissionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuLayoutServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuLayoutService menuLayoutService;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUsername("testuser");

        RoleEntity role1 = new RoleEntity("PROCESS_OWNER", "Owner");
        role1.setPermissions(Set.of(
                new PermissionEntity("MENU_DASHBOARD", "Dash"),
                new PermissionEntity("MENU_WORKDESK", "Workdesk")
        ));

        RoleEntity role2 = new RoleEntity("AUDITOR", "Auditor");
        role2.setPermissions(Set.of(
                new PermissionEntity("MENU_WORKDESK", "Workdesk"),
                new PermissionEntity("MENU_REPORTS", "Reports")
        ));

        mockUser.setRoles(Set.of(role1, role2));
    }

    @Test
    void testComputeTopology_WithoutDuplicates() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        Set<String> result = menuLayoutService.computeTopologyForUser("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("WORKDESK"));
    }

    @Test
    void testComputeTopology_NativeAdminBypass() {
        UserEntity adminUser = new UserEntity();
        adminUser.setUsername("super_admin");
        RoleEntity adminRole = new RoleEntity("SUPER_ADMIN", "Admin");
        adminUser.setRoles(Set.of(adminRole));

        when(userRepository.findByUsername("super_admin")).thenReturn(Optional.of(adminUser));

        Set<String> result = menuLayoutService.computeTopologyForUser("super_admin");

        assertNotNull(result);
        assertEquals(7, result.size());
        assertTrue(result.contains("WORKDESK"));
        assertTrue(result.contains("ADMINISTRATION"));
    }

    @Test
    void testImmutability_ThrowsAccessDenied_ForNativeRoles() {
        RoleEntity nativeRole = new RoleEntity("SUPER_ADMIN", "Admin");
        
        assertThrows(AccessDeniedException.class, () -> {
            menuLayoutService.validateRoleImmutability(nativeRole);
        });
    }
}
