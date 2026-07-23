package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * US-036 CA-6: Tests unitarios para la validación de ciclos y resolución jerárquica.
 */
@ExtendWith(MockitoExtension.class)
class RoleHierarchyServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleHierarchyService service;

    private UUID gerenteId;
    private UUID coordinadorId;
    private UUID analistaId;

    @BeforeEach
    void setUp() {
        gerenteId = UUID.randomUUID();
        coordinadorId = UUID.randomUUID();
        analistaId = UUID.randomUUID();
    }

    // ── Resolución efectiva (directos + heredados) ─────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: Acumula roles directos + heredados sin duplicados")
    void resolveAllEffectiveRoles_CombinesDirectAndInherited() {
        when(roleRepository.findInheritedRoleNamesByName("ROLE_ANALISTA"))
                .thenReturn(List.of("ROLE_COORDINADOR", "ROLE_GERENTE"));

        Set<String> effective = service.resolveAllEffectiveRoles(Set.of("ROLE_ANALISTA"));

        assertThat(effective).containsExactlyInAnyOrder("ROLE_ANALISTA", "COORDINADOR", "GERENTE");
    }
}
