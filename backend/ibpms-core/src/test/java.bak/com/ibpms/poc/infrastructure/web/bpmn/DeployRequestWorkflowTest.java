package com.ibpms.poc.infrastructure.web.bpmn;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class DeployRequestWorkflowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ibpms_test")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

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
