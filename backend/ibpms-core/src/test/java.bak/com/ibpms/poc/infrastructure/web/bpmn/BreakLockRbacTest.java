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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_process_locks CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_audit_log CASCADE");
    }

    private void seedLock(String processKey, String user) {
        jdbcTemplate.update(
            "INSERT INTO ibpms_process_locks (process_definition_key, locked_by, locked_at, browser_session_id) VALUES (?, ?, ?, ?)",
            processKey, user, LocalDateTime.now(), "session123"
        );
    }

    @Test
    @DisplayName("CA-64: testBreakLockWithSuperAdminReturns200")
    void testBreakLockWithSuperAdminReturns200() {
        seedLock("broken-process", "another-user");

        given()
            .header("X-Mock-Role", "SUPER_ADMIN") // Simulating Auth/RBAC interceptor mock
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
            .header("X-Mock-Role", "PROCESS_DESIGNER")
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
            .header("X-Mock-Role", "SUPER_ADMIN")
            .header("X-Mock-User", "admin-user")
            .contentType(ContentType.JSON)
        .when()
            .delete("/audited-process/lock/force")
        .then()
            .statusCode(200);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_audit_log WHERE event_data::text LIKE '%force%' AND entity_type = 'BPMN_PROCESS'", Integer.class);
        
        assertTrue(count != null && count > 0, "Debe crearse un log indicando que se rompió el lock forzosamente");
    }
}
