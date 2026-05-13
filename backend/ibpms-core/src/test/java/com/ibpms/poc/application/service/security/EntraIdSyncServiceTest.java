package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.service.security.exceptions.PreconditionRequiredException;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @Traceability(US = "US-038", CA = "CA-03")
 * TDD: Validación de Aprovisionamiento JIT con Claims Vitales y HTTP 428.
 */
class EntraIdSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private EntraIdSyncService entraIdSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void provisionUser_shouldThrowPreconditionRequiredException_whenClaimsMissing() {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", "test@test.com"); // Falta name, Sucursal_ID, Codigo_Jefe
        
        PreconditionRequiredException exception = assertThrows(PreconditionRequiredException.class, () -> {
            entraIdSyncService.provisionUser("testuser", claims);
        });

        assertEquals("Claims obligatorios faltantes para JIT Provisioning.", exception.getMessage());
        assertTrue(exception.getMissingFields().contains("name"));
        assertTrue(exception.getMissingFields().contains("Sucursal_ID"));
        assertTrue(exception.getMissingFields().contains("Codigo_Jefe"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void provisionUser_shouldProvisionSuccessfully_andRecordJitClaimsJson() {
        Map<String, String> claims = new HashMap<>();
        claims.put("email", "test@test.com");
        claims.put("name", "Test User");
        claims.put("Sucursal_ID", "SUC-001");
        claims.put("Codigo_Jefe", "J-002");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER_INTERNAL")).thenReturn(Optional.of(new RoleEntity("ROLE_USER_INTERNAL", "Desc")));
        
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity u = invocation.getArgument(0);
            return u;
        });

        UserEntity user = entraIdSyncService.provisionUser("testuser", claims);

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertTrue(user.getJitClaimsJson().contains("SUC-001"));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
