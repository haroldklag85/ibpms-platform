package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.web.server.LocalServerPort;


public class DeployRequestWorkflowTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/deploy-requests";
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ibpms_deploy_requests (id VARCHAR(255) PRIMARY KEY, status VARCHAR(50), requested_by VARCHAR(255))");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_deploy_requests");
    }

    @Test
    @DisplayName("CA-69: testRequestDeployCreatesPendingRecord")
    void testRequestDeployCreatesPendingRecord() {
        given()
            .header("X-Mock-Role", "PROCESS_DESIGNER")
            .multiPart("file", "test.bpmn", "<bpmn/>".getBytes())
            .multiPart("comment", "Requesting deploy for production")
        .when()
            .post("/")
        .then()
            .statusCode(201)
            .body("status", equalTo("PENDING"));
            
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_deploy_requests WHERE status = 'PENDING'", Integer.class);
        assertEquals(1, count, "Debe existir una solicitud de despliegue en estado PENDING");
    }

    @Test
    @DisplayName("CA-69: testRejectWithoutCommentReturns400")
    void testRejectWithoutCommentReturns400() {
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by) VALUES (?, ?, ?)", "req-sub1", "PENDING", "user1");

        given()
            .header("X-Mock-Role", "SUPER_ADMIN")
            .contentType(ContentType.JSON)
            .body("{}") // Missing comment
        .when()
            .post("/req-sub1/reject")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-69: testApproveTriggersDeployment")
    void testApproveTriggersDeployment() {
        jdbcTemplate.update("INSERT INTO ibpms_deploy_requests (id, status, requested_by) VALUES (?, ?, ?)", "req-sub2", "PENDING", "user1");

        given()
            .header("X-Mock-Role", "SUPER_ADMIN")
            .contentType(ContentType.JSON)
            .body("{\"comment\": \"Approved flow\"}")
        .when()
            .post("/req-sub2/approve")
        .then()
            .statusCode(200)
            .body("deployment_status", equalTo("SUCCESS"));
            
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM ibpms_deploy_requests WHERE id = 'req-sub2'", String.class);
        assertEquals("APPROVED", status, "El request debe quedar como APPROVED");
    }
}
