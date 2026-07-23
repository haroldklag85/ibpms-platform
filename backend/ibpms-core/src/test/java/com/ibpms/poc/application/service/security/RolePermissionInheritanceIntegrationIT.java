// @Traceability: US-005, CA-41 - ADR-001
package com.ibpms.poc.application.service.security;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.infrastructure.jpa.entity.security.ProcessPermissionEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-036 CA-6 — Resolución de Árbol de Permisos por Herencia Piramidal (Testcontainers)
 *
 * Certifica con PostgreSQL real (pgvector:pg16) que:
 * - CA-6 BE-01: Un rol con parentRole hereda los ProcessPermissions de su padre
 * - CA-6 BE-02: La herencia es transitiva (nieto hereda del abuelo)
 * - CA-6 BE-03: Un rol sin padre devuelve solo sus propios permisos
 * - CA-6 BE-04: createRole resuelve parentRole por ID correctamente
 * - CA-6 BE-05: createRole lanza excepción si parentRole.id no existe
 */
@DisplayName("US-036 CA-6 — Herencia Piramidal de Permisos (WITH RECURSIVE)")
@org.springframework.transaction.annotation.Transactional
class RolePermissionInheritanceIntegrationIT extends AbstractIntegrationIT {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID grandparentRoleId;
    private UUID parentRoleId;
    private UUID childRoleId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM ibpms_security_user_roles");
        jdbcTemplate.execute("UPDATE ibpms_security_role SET parent_role_id = NULL");
        roleRepository.deleteAll();
        roleRepository.flush();

        // Tier 1 — Abuelo: tiene permiso en "proceso-contrato"
        RoleEntity grandparent = new RoleEntity("ROLE_DIRECTOR", "Director con máximos permisos");
        grandparent.setIsTemplate(false);
        grandparent.setSource("LOCAL");
        grandparentRoleId = roleRepository.save(grandparent).getId();

        // Inyectar ProcessPermission al abuelo directamente
        RoleEntity gp = roleRepository.findById(grandparentRoleId).orElseThrow();
        ProcessPermissionEntity gpPerm = new ProcessPermissionEntity(
                "proceso-contrato", true, true, gp);
        gp.getProcessPermissions().add(gpPerm);
        roleRepository.save(gp);

        // Tier 2 — Padre: tiene permiso en "proceso-solicitud", hereda de abuelo
        RoleEntity parent = new RoleEntity("ROLE_GERENTE", "Gerente intermedio");
        parent.setIsTemplate(false);
        parent.setSource("LOCAL");
        parent.setParentRole(gp);
        parentRoleId = roleRepository.save(parent).getId();

        RoleEntity p = roleRepository.findById(parentRoleId).orElseThrow();
        ProcessPermissionEntity parentPerm = new ProcessPermissionEntity(
                "proceso-solicitud", true, false, p);
        p.getProcessPermissions().add(parentPerm);
        roleRepository.save(p);

        // Tier 3 — Hijo: solo tiene permiso en "proceso-tarea", hereda de padre (y abuelo transitivamente)
        RoleEntity child = new RoleEntity("ROLE_ANALISTA", "Analista operativo");
        child.setIsTemplate(false);
        child.setSource("LOCAL");
        child.setParentRole(p);
        childRoleId = roleRepository.save(child).getId();

        RoleEntity c = roleRepository.findById(childRoleId).orElseThrow();
        ProcessPermissionEntity childPerm = new ProcessPermissionEntity(
                "proceso-tarea", false, true, c);
        c.getProcessPermissions().add(childPerm);
        roleRepository.save(c);
    }

    // ─────────────────────────────────────────────
    // CA-6: Herencia directa (hijo → padre)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-6 RBAC-BE-08: hijo hereda ProcessPermissions del padre directamente")
    void testDirectInheritance() {
        List<ProcessPermissionEntity> effective = roleService.getEffectiveProcessPermissions(parentRoleId);

        // El padre debe tener sus propios + los del abuelo
        List<String> keys = effective.stream()
                .map(ProcessPermissionEntity::getProcessDefinitionKey)
                .toList();

        assertThat(keys).contains("proceso-solicitud", "proceso-contrato");
    }

    // ─────────────────────────────────────────────
    // CA-6: Herencia transitiva (nieto → padre → abuelo)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-6 RBAC-BE-09: herencia transitiva — nieto obtiene permisos de padre y abuelo")
    void testTransitiveInheritance() {
        List<ProcessPermissionEntity> effective = roleService.getEffectiveProcessPermissions(childRoleId);

        List<String> keys = effective.stream()
                .map(ProcessPermissionEntity::getProcessDefinitionKey)
                .toList();

        // El analista (nieto) debe ver los 3 permisos: propio + padre + abuelo
        assertThat(keys).containsExactlyInAnyOrder(
                "proceso-tarea",
                "proceso-solicitud",
                "proceso-contrato"
        );
    }

    // ─────────────────────────────────────────────
    // CA-6: Rol raíz sin padre — solo sus propios permisos
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-6 RBAC-BE-10: rol sin padre devuelve únicamente sus propios permisos")
    void testRootRoleNoInheritance() {
        List<ProcessPermissionEntity> effective = roleService.getEffectiveProcessPermissions(grandparentRoleId);

        List<String> keys = effective.stream()
                .map(ProcessPermissionEntity::getProcessDefinitionKey)
                .toList();

        assertThat(keys).containsExactly("proceso-contrato");
        assertThat(keys).doesNotContain("proceso-solicitud", "proceso-tarea");
    }

    // ─────────────────────────────────────────────
    // CA-6: createRole resuelve parentRole por ID
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-6 RBAC-BE-11: createRole resuelve parentRole por UUID correctamente")
    void testCreateRoleWithParentResolution() {
        RoleEntity newRole = new RoleEntity("ROLE_JUNIOR", "Analista junior");
        newRole.setIsTemplate(false);
        newRole.setSource("LOCAL");

        RoleEntity parentRef = new RoleEntity();
        parentRef.setId(childRoleId); // solo el ID — Jackson/REST pattern
        newRole.setParentRole(parentRef);

        RoleEntity saved = roleService.createRole(newRole);

        assertThat(saved.getParentRole()).isNotNull();
        assertThat(saved.getParentRole().getId()).isEqualTo(childRoleId);
        assertThat(saved.getParentRole().getName()).isEqualTo("ROLE_ANALISTA");
    }

    @Test
    @DisplayName("CA-6 RBAC-BE-12: createRole lanza IllegalArgumentException para parentRole.id inexistente")
    void testCreateRoleWithPhantomParentThrows() {
        RoleEntity newRole = new RoleEntity("ROLE_FANTASMA", "Rol con padre inexistente");
        newRole.setIsTemplate(false);
        newRole.setSource("LOCAL");

        RoleEntity phantomParent = new RoleEntity();
        phantomParent.setId(UUID.randomUUID());
        newRole.setParentRole(phantomParent);

        assertThatThrownBy(() -> roleService.createRole(newRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol padre no encontrado");
    }
}
