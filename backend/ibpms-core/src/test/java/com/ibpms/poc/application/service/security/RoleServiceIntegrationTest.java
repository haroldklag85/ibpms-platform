package com.ibpms.poc.application.service.security;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-036 CA-2 & CA-3 — Testcontainers Integration Tests
 *
 * Verifica con PostgreSQL real (pgvector:pg16) que:
 * - CA-2: deleteRole y updateRole lanzan AccessDeniedException para ROLE_SUPER_ADMIN
 * - CA-2: deleteRole funciona correctamente para roles no-Root
 * - CA-3: assignTemplateToUsers asigna el rol a los usuarios en una sola transacción
 * - CA-3: assignTemplateToUsers rechaza roles que no son plantilla
 */
@DisplayName("US-036 RBAC — RoleService Guard & Mass Assignment (Testcontainers)")
class RoleServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID rootRoleId;
    private UUID regularRoleId;
    private UUID templateRoleId;

    @BeforeEach
    void setUp() {
        // Limpiar estado previo (orden inverso para respetar FK)
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed: Rol Guardián Root
        RoleEntity root = new RoleEntity(RoleService.ROOT_ROLE, "Guardián de seguridad — inmutable");
        root.setIsTemplate(false);
        root.setSource("LOCAL");
        rootRoleId = roleRepository.save(root).getId();

        // Seed: Rol regular eliminable
        RoleEntity regular = new RoleEntity("ROLE_ANALYST", "Analista operativo");
        regular.setIsTemplate(false);
        regular.setSource("LOCAL");
        regularRoleId = roleRepository.save(regular).getId();

        // Seed: Rol plantilla para mass assign
        RoleEntity template = new RoleEntity("ROLE_ONBOARDING_TEMPLATE", "Plantilla para nuevos empleados");
        template.setIsTemplate(true);
        template.setSource("LOCAL");
        templateRoleId = roleRepository.save(template).getId();
    }

    // ─────────────────────────────────────────────
    // CA-2: Inmutabilidad del Guardián — deleteRole
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-2 RBAC-BE-01: deleteRole lanza AccessDeniedException para ROLE_SUPER_ADMIN")
    void testPreventSuperAdminRoleDeletion() {
        assertThatThrownBy(() -> roleService.deleteRole(rootRoleId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Mutación/Borrado de Rol Root prohibido por diseño de seguridad.");

        // El rol Root debe permanecer intacto en la BD
        assertThat(roleRepository.findById(rootRoleId)).isPresent();
    }

    @Test
    @DisplayName("CA-2 RBAC-BE-02: deleteRole elimina correctamente un rol no-Root (Soft Delete)")
    void testDeleteNonRootRoleSucceeds() {
        roleService.deleteRole(regularRoleId);

        RoleEntity deletedRole = roleRepository.findById(regularRoleId).orElseThrow();
        assertThat(deletedRole.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("CA-2 RBAC-BE-03: updateRole lanza AccessDeniedException para ROLE_SUPER_ADMIN")
    void testPreventSuperAdminRoleUpdate() {
        RoleEntity patch = new RoleEntity();
        patch.setDescription("Intento malicioso de modificar el Root");

        assertThatThrownBy(() -> roleService.updateRole(rootRoleId, patch))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Mutación/Borrado de Rol Root prohibido por diseño de seguridad.");

        // Descripción original debe permanecer sin cambios
        RoleEntity unchanged = roleRepository.findById(rootRoleId).orElseThrow();
        assertThat(unchanged.getDescription()).isEqualTo("Guardián de seguridad — inmutable");
    }

    @Test
    @DisplayName("CA-2 RBAC-BE-04: deleteRole lanza IllegalArgumentException para ID inexistente")
    void testDeleteNonExistentRoleThrows() {
        UUID phantom = UUID.randomUUID();
        assertThatThrownBy(() -> roleService.deleteRole(phantom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    // ─────────────────────────────────────────────
    // CA-3: Asignación Masiva desde Plantilla
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CA-3 RBAC-BE-05: assignTemplateToUsers asigna el rol plantilla en una transacción")
    void testMassAssignTemplateToUsers() {
        // Seed usuarios
        UserEntity u1 = buildUser("ana@empresa.com");
        UserEntity u2 = buildUser("pedro@empresa.com");
        UUID u1Id = userRepository.save(u1).getId();
        UUID u2Id = userRepository.save(u2).getId();

        List<UUID> notFound = roleService.assignTemplateToUsers(templateRoleId, List.of(u1Id, u2Id));

        assertThat(notFound).isEmpty();

        // Verificar que ambos usuarios tienen el rol plantilla asignado
        UserEntity saved1 = userRepository.findById(u1Id).orElseThrow();
        UserEntity saved2 = userRepository.findById(u2Id).orElseThrow();
        assertThat(saved1.getRoles()).anyMatch(r -> "ROLE_ONBOARDING_TEMPLATE".equals(r.getName()));
        assertThat(saved2.getRoles()).anyMatch(r -> "ROLE_ONBOARDING_TEMPLATE".equals(r.getName()));
    }

    @Test
    @DisplayName("CA-3 RBAC-BE-06: assignTemplateToUsers devuelve IDs no encontrados sin abortar la transacción")
    void testMassAssignReturnsNotFoundIds() {
        UserEntity u1 = buildUser("valida@empresa.com");
        UUID u1Id = userRepository.save(u1).getId();
        UUID phantomId = UUID.randomUUID();

        List<UUID> notFound = roleService.assignTemplateToUsers(templateRoleId, List.of(u1Id, phantomId));

        assertThat(notFound).containsExactly(phantomId);
        // El usuario válido sí recibió el rol
        UserEntity saved = userRepository.findById(u1Id).orElseThrow();
        assertThat(saved.getRoles()).anyMatch(r -> "ROLE_ONBOARDING_TEMPLATE".equals(r.getName()));
    }

    @Test
    @DisplayName("CA-3 RBAC-BE-07: assignTemplateToUsers rechaza roles sin isTemplate=true")
    void testMassAssignRejectsNonTemplateRole() {
        UserEntity u1 = buildUser("operario@empresa.com");
        UUID u1Id = userRepository.save(u1).getId();

        assertThatThrownBy(() -> roleService.assignTemplateToUsers(regularRoleId, List.of(u1Id)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no es una plantilla asignable");
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private UserEntity buildUser(String email) {
        UserEntity u = new UserEntity();
        u.setUsername(email.split("@")[0]);
        u.setEmail(email);
        u.setPasswordHash("$2a$10$placeholder_hash");
        u.setIsActive(true);
        u.setIsExternalIdp(false);
        return u;
    }
}
