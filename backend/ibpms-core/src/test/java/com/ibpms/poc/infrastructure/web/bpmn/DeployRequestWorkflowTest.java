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
import org.springframework.http.HttpStatus;

import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

// @Traceability: US-005, CA-69 (Reemplazo DDL mock por Liquibase Testcontainer)
@Traceability(US = "US-005", CA = {"CA-69"})
public class DeployRequestWorkflowTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_deploy_requests CASCADE");
        // Ensure there is a process so FKs don't fail and service can find it
        jdbcTemplate.execute("INSERT INTO ibpms_bpmn_process_design (id, technical_id, name, form_pattern, created_by, status, current_version, created_at, updated_at, max_nodes) VALUES ('123e4567-e89b-12d3-a456-426614174000', 'test-process-deploy', 'test', 'SIMPLE', 'system', 'DRAFT', 1, NOW(), NOW(), 100) ON CONFLICT DO NOTHING");
        // Ensure user exists in database to avoid JIT sync or soft-delete failures
        jdbcTemplate.execute("INSERT INTO ibpms_security_user (id, username, email, status, is_external_idp, created_at, must_change_password) VALUES (gen_random_uuid(), 'user1', 'user1@local.com', 'ACTIVE', false, CURRENT_TIMESTAMP, false) ON CONFLICT DO NOTHING");
    }

    @org.junit.jupiter.api.AfterEach
    void resetRestAssured() {
        RestAssured.reset();
    }

    private String getValidToken() {
        return jwtTokenProvider.generateToken("user1", java.util.Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "tenant_alpha");
    }

    @Test
    @DisplayName("CA-69: testRequestDeployCreatesPendingRecord")
    void testRequestDeployCreatesPendingRecord() {
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn2:process id=\"test-process-deploy\" isExecutable=\"true\">\n" +
                "    <bpmn2:startEvent id=\"StartEvent_1\" />\n" +
                "  </bpmn2:process>\n" +
                "</bpmn2:definitions>";

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .multiPart("file", "test.bpmn", bpmnContent.getBytes(), "text/xml")
        .when()
            .post("/deploy-request")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("status", equalTo("PENDING_APPROVAL"));
            
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_deploy_requests WHERE status = 'PENDING'", Integer.class);
        assertEquals(1, count, "Debe existir una solicitud de despliegue en estado PENDING");
    }

    @Test
    @DisplayName("CA-69: testRejectWithoutCommentReturns400")
    void testRejectWithoutCommentReturns400() {
        UUID reqId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by, requested_at, process_definition_key, xml_payload) VALUES (?::uuid, ?, ?, ?, ?, ?)", reqId.toString(), "PENDING", "user1", LocalDateTime.now(), "test-process-deploy", "<bpmn2:definitions/>");

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .contentType(ContentType.JSON)
            .body("{\"approved\": false, \"comment\": \"short\"}") // Missing or short comment
        .when()
            .post("/deploy-requests/" + reqId + "/review")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value()); // Because comment < 20 chars
    }

    @Test
    @DisplayName("CA-69: testApproveTriggersDeployment")
    void testApproveTriggersDeployment() {
        UUID reqId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by, requested_at, process_definition_key, xml_payload) VALUES (?::uuid, ?, ?, ?, ?, ?)", reqId.toString(), "PENDING", "user1", LocalDateTime.now(), "test-process-deploy", "<bpmn2:definitions/>");

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .contentType(ContentType.JSON)
            .body("{\"approved\": true, \"comment\": \"Approved flow for production usage\"}")
        .when()
            .post("/deploy-requests/" + reqId + "/review")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("status", equalTo("APPROVED"));
            
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM ibpms_deploy_requests WHERE id = ?::uuid", String.class, reqId.toString());
        assertEquals("APPROVED", status, "El request debe quedar como APPROVED");
    }
}
