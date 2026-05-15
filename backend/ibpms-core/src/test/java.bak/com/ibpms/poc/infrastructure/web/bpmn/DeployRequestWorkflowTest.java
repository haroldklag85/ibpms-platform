package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-005, CA-69 (Reemplazo DDL mock por Liquibase Testcontainer)
@Traceability(US = "US-005", CA = {"CA-69"})
public class DeployRequestWorkflowTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes/deploy-requests";
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_deploy_requests CASCADE");
        // Ensure there is a process so FKs don't fail and service can find it
        jdbcTemplate.execute("INSERT INTO ibpms_bpmn_process_design (id, technical_id, name, form_pattern, created_by, status, current_version, created_at, updated_at, max_nodes) VALUES ('123e4567-e89b-12d3-a456-426614174000', 'test-process-deploy', 'test', 'SIMPLE', 'system', 'DRAFT', 1, NOW(), NOW(), 100) ON CONFLICT DO NOTHING");
    }

    @org.junit.jupiter.api.AfterEach
    void resetRestAssured() {
        RestAssured.reset();
    }

    @Test
    @DisplayName("CA-69: testRequestDeployCreatesPendingRecord")
    void testRequestDeployCreatesPendingRecord() {
        // The endpoint is actually POST /deploy-requests where payload has processDefinitionKey
        given()
            .header("X-Mock-Role", "PROCESS_DESIGNER")
            .header("X-Mock-User", "user1")
            .contentType(ContentType.JSON)
            .body("{\"processDefinitionKey\": \"test-process-deploy\"}")
        .when()
            .post("/")
        .then()
            .statusCode(200)
            .body("status", equalTo("PENDING_APPROVAL"));
            
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_deploy_requests WHERE status = 'PENDING'", Integer.class);
        assertEquals(1, count, "Debe existir una solicitud de despliegue en estado PENDING");
    }

    @Test
    @DisplayName("CA-69: testRejectWithoutCommentReturns400")
    void testRejectWithoutCommentReturns400() {
        UUID reqId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by, requested_at, process_definition_key) VALUES (?::uuid, ?, ?, ?, ?)", reqId.toString(), "PENDING", "user1", LocalDateTime.now(), "test-process-deploy");

        given()
            .header("X-Mock-Role", "SUPER_ADMIN")
            .contentType(ContentType.JSON)
            .body("{\"comment\": \"short\"}") // Missing or short comment
        .when()
            .post("/" + reqId + "/reject")
        .then()
            .statusCode(400); // Because comment < 20 chars
    }

    @Test
    @DisplayName("CA-69: testApproveTriggersDeployment")
    void testApproveTriggersDeployment() {
        UUID reqId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by, requested_at, process_definition_key) VALUES (?::uuid, ?, ?, ?, ?)", reqId.toString(), "PENDING", "user1", LocalDateTime.now(), "test-process-deploy");

        given()
            .header("X-Mock-Role", "SUPER_ADMIN")
            .contentType(ContentType.JSON)
            .body("{\"comment\": \"Approved flow for production usage\"}")
        .when()
            .post("/" + reqId + "/approve")
        .then()
            .statusCode(200)
            .body("status", equalTo("APPROVED"));
            
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM ibpms_deploy_requests WHERE id = ?::uuid", String.class, reqId.toString());
        assertEquals("APPROVED", status, "El request debe quedar como APPROVED");
    }
}
