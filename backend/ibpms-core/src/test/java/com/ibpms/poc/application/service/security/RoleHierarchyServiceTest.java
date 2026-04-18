package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleHierarchyRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleTemplateRepository;
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
    private RoleHierarchyRepository hierarchyRepository;

    @Mock
    private RoleTemplateRepository roleTemplateRepository;

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

    // ── Guardia 1: Auto-referencia ──────────────────────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: RECHAZA auto-referencia (Rol no puede ser padre de sí mismo)")
    void registerHierarchy_SelfReference_ThrowsCycleException() {
        assertThatThrownBy(() -> service.registerHierarchy(gerenteId, gerenteId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ciclo detectado");
    }

    // ── Guardia 2: Relación inversa directa ─────────────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: RECHAZA relación inversa directa (A→B ya existe, se intenta B→A)")
    void registerHierarchy_InverseRelation_ThrowsCycleException() {
        when(hierarchyRepository.existsByParentRoleIdAndChildRoleId(coordinadorId, gerenteId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.registerHierarchy(gerenteId, coordinadorId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("relación inversa");
    }

    // ── Guardia 3: Ciclo indirecto vía CTE ──────────────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: RECHAZA ciclo indirecto (A→B→C, se intenta C→A)")
    void registerHierarchy_IndirectCycle_ThrowsCycleException() {
        // El child (analistaId) ya es ancestro del parent (gerenteId)
        when(hierarchyRepository.existsByParentRoleIdAndChildRoleId(analistaId, gerenteId))
                .thenReturn(false);
        when(hierarchyRepository.findAllAncestorRoleIds(gerenteId))
                .thenReturn(List.of(analistaId)); // analistaId es ancestro de gerenteId

        assertThatThrownBy(() -> service.registerHierarchy(gerenteId, analistaId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ciclo indirecto");
    }

    // ── Caso Feliz: Registro exitoso ────────────────────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: Registra herencia válida Gerente→Coordinador sin ciclo")
    void registerHierarchy_ValidRelation_PersistsSuccessfully() {
        when(hierarchyRepository.existsByParentRoleIdAndChildRoleId(coordinadorId, gerenteId))
                .thenReturn(false);
        when(hierarchyRepository.findAllAncestorRoleIds(gerenteId))
                .thenReturn(Collections.emptyList());
        when(hierarchyRepository.existsByParentRoleIdAndChildRoleId(gerenteId, coordinadorId))
                .thenReturn(false);

        RoleTemplateEntity parentTemplate = new RoleTemplateEntity();
        parentTemplate.setId(gerenteId);
        parentTemplate.setRoleName("GERENTE_RIESGOS");
        when(roleTemplateRepository.findById(gerenteId)).thenReturn(Optional.of(parentTemplate));

        RoleTemplateEntity childTemplate = new RoleTemplateEntity();
        childTemplate.setId(coordinadorId);
        childTemplate.setRoleName("COORDINADOR_RIESGOS");
        when(roleTemplateRepository.findById(coordinadorId)).thenReturn(Optional.of(childTemplate));

        RoleHierarchyEntity savedEntity = new RoleHierarchyEntity();
        savedEntity.setParentRole(parentTemplate);
        savedEntity.setChildRole(childTemplate);
        when(hierarchyRepository.save(any())).thenReturn(savedEntity);

        RoleHierarchyEntity result = service.registerHierarchy(gerenteId, coordinadorId);

        assertThat(result.getParentRole().getRoleName()).isEqualTo("GERENTE_RIESGOS");
        assertThat(result.getChildRole().getRoleName()).isEqualTo("COORDINADOR_RIESGOS");
        verify(hierarchyRepository, times(1)).save(any());
    }

    // ── Resolución de herencia (3 niveles) ──────────────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: Resuelve herencia piramidal de 3 niveles (Analista→Coordinador→Gerente)")
    void resolveInheritedRoleNames_ThreeLevels_ReturnsAllAncestors() {
        when(hierarchyRepository.findAllAncestorRoleIds(analistaId))
                .thenReturn(List.of(coordinadorId, gerenteId));

        RoleTemplateEntity coord = new RoleTemplateEntity();
        coord.setId(coordinadorId);
        coord.setRoleName("COORDINADOR");

        RoleTemplateEntity gerente = new RoleTemplateEntity();
        gerente.setId(gerenteId);
        gerente.setRoleName("GERENTE");

        when(roleTemplateRepository.findAllById(List.of(coordinadorId, gerenteId)))
                .thenReturn(List.of(coord, gerente));

        List<String> inherited = service.resolveInheritedRoleNames(analistaId);

        assertThat(inherited).containsExactlyInAnyOrder("COORDINADOR", "GERENTE");
    }

    // ── Resolución efectiva (directos + heredados) ─────────────────────────────

    @Test
    @DisplayName("US-036 CA-6: Acumula roles directos + heredados sin duplicados")
    void resolveAllEffectiveRoles_CombinesDirectAndInherited() {
        RoleTemplateEntity analistaTemplate = new RoleTemplateEntity();
        analistaTemplate.setId(analistaId);
        analistaTemplate.setRoleName("ANALISTA");

        when(roleTemplateRepository.findByRoleName("ANALISTA"))
                .thenReturn(Optional.of(analistaTemplate));
        when(hierarchyRepository.findAllAncestorRoleIds(analistaId))
                .thenReturn(List.of(coordinadorId, gerenteId));

        RoleTemplateEntity coord = new RoleTemplateEntity();
        coord.setId(coordinadorId);
        coord.setRoleName("COORDINADOR");

        RoleTemplateEntity gerente = new RoleTemplateEntity();
        gerente.setId(gerenteId);
        gerente.setRoleName("GERENTE");

        when(roleTemplateRepository.findAllById(List.of(coordinadorId, gerenteId)))
                .thenReturn(List.of(coord, gerente));

        Set<String> effective = service.resolveAllEffectiveRoles(Set.of("ANALISTA"));

        assertThat(effective).containsExactlyInAnyOrder("ANALISTA", "COORDINADOR", "GERENTE");
    }
}
