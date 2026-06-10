package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-005, CA-64 (Reemplazo DDL mock por Liquibase Testcontainer)
@Traceability(US = "US-005", CA = {"CA-64"})
public class BreakLockRbacTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.ibpms.poc.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository userRepository;

    @Autowired
    private com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository roleRepository;

    private com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity superAdminUser;
    private com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity designerUser;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_process_locks CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_audit_log CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_bpmn_process_design CASCADE");

        seedProcessDesign("broken-process", "Broken Process");
        seedProcessDesign("secured-process", "Secured Process");
        seedProcessDesign("audited-process", "Audited Process");
        seedProcessDesign("active-process-lock", "Active Process");
        seedProcessDesign("non-existent-lock", "Non Existent Lock Process");

        // Seed roles and users
        java.util.Optional<com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity> existingAdmin = userRepository.findByUsername("super_admin_test");
        if (existingAdmin.isPresent()) {
            superAdminUser = existingAdmin.get();
        } else {
            com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity adminRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(new com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity("ROLE_SUPER_ADMIN", "Super Admin")));

            superAdminUser = new com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity();
            superAdminUser.setUsername("super_admin_test");
            superAdminUser.setEmail("admin@test.com");
            superAdminUser.getRoles().add(adminRole);
            superAdminUser = userRepository.save(superAdminUser);
        }

        java.util.Optional<com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity> existingDesigner = userRepository.findByUsername("designer_test");
        if (existingDesigner.isPresent()) {
            designerUser = existingDesigner.get();
        } else {
            com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity designerRole = roleRepository.findByName("ROLE_PROCESS_DESIGNER")
                .orElseGet(() -> roleRepository.save(new com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity("ROLE_PROCESS_DESIGNER", "Process Designer")));

            designerUser = new com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity();
            designerUser.setUsername("designer_test");
            designerUser.setEmail("designer@test.com");
            designerUser.getRoles().add(designerRole);
            designerUser = userRepository.save(designerUser);
        }
    }

    private String getSuperAdminToken() {
        return jwtTokenProvider.generateToken(superAdminUser.getUsername(), java.util.Collections.singletonList("ROLE_SUPER_ADMIN"), "T1");
    }

    private String getDesignerToken() {
        return jwtTokenProvider.generateToken(designerUser.getUsername(), java.util.Collections.singletonList("ROLE_PROCESS_DESIGNER"), "T1");
    }

    private void seedLock(String processKey, String user) {
        jdbcTemplate.update(
            "INSERT INTO ibpms_process_locks (process_definition_key, locked_by, locked_at, browser_session_id) VALUES (?, ?, ?, ?)",
            processKey, user, LocalDateTime.now(), "session123"
        );
    }

    private void seedProcessDesign(String processKey, String name) {
        jdbcTemplate.update(
            "INSERT INTO ibpms_bpmn_process_design (id, name, technical_id, form_pattern, status, current_version, max_nodes, created_at, updated_at, created_by, is_public) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            java.util.UUID.randomUUID(), name, processKey, "SIMPLE", "DRAFT", 1, 50, LocalDateTime.now(), LocalDateTime.now(), "admin", false
        );
    }

    @Test
    @DisplayName("CA-64: testBreakLockWithSuperAdminReturns200")
    void testBreakLockWithSuperAdminReturns200() {
        seedLock("broken-process", "another-user");

        given()
            .header("Authorization", "Bearer " + getSuperAdminToken())
            .contentType(ContentType.JSON)
        .when()
            .delete("/broken-process/lock/force")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("CA-64: testBreakLockWithoutSuperAdminReturns403")
    void testBreakLockWithoutSuperAdminReturns403() {
        seedLock("secured-process", "another-user");

        given()
            .header("Authorization", "Bearer " + getDesignerToken())
            .contentType(ContentType.JSON)
        .when()
            .delete("/secured-process/lock/force")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("CA-64: testBreakLockCreatesAuditEntry")
    void testBreakLockCreatesAuditEntry() {
        seedLock("audited-process", "another-user");

        given()
            .header("Authorization", "Bearer " + getSuperAdminToken())
            .contentType(ContentType.JSON)
        .when()
            .delete("/audited-process/lock/force")
        .then()
            .statusCode(200);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_audit_log WHERE event_data::text LIKE '%force%' AND entity_type = 'BPMN_PROCESS'", Integer.class);
        
        assertTrue(count != null && count > 0, "Debe crearse un log indicando que se rompió el lock forzosamente");
    }

    @Test
    @DisplayName("CA-16: testGetLockWhenNotLockedReturnsActiveFalse")
    void testGetLockWhenNotLockedReturnsActiveFalse() {
        given()
            .header("Authorization", "Bearer " + getDesignerToken())
            .contentType(ContentType.JSON)
        .when()
            .get("/non-existent-lock/lock")
        .then()
            .statusCode(200)
            .body("active", org.hamcrest.Matchers.equalTo(false));
    }

    @Test
    @DisplayName("CA-16: testGetLockWhenLockedReturnsActiveTrueAndDetails")
    void testGetLockWhenLockedReturnsActiveTrueAndDetails() {
        seedLock("active-process-lock", "some-designer");
        given()
            .header("Authorization", "Bearer " + getDesignerToken())
            .contentType(ContentType.JSON)
        .when()
            .get("/active-process-lock/lock")
        .then()
            .statusCode(200)
            .body("active", org.hamcrest.Matchers.equalTo(true))
            .body("owner", org.hamcrest.Matchers.equalTo("some-designer"))
            .body("since", org.hamcrest.Matchers.notNullValue());
    }
}
