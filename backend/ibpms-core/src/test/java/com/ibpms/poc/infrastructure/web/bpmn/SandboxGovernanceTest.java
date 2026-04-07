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

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SandboxGovernanceTest {

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
        RestAssured.basePath = "/api/v1/design/sandbox";
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ibpms_sandbox_instances (id VARCHAR(255) PRIMARY KEY, created_by VARCHAR(255), created_at TIMESTAMP)");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_sandbox_instances");
    }

    @Test
    @DisplayName("CA-67: testMaxThreeConcurrentSandboxInstances")
    void testMaxThreeConcurrentSandboxInstances() {
        // Create 3 instances directly in db to simulate state
        jdbcTemplate.update("INSERT INTO ibpms_sandbox_instances (id, created_by, created_at) VALUES (?, ?, ?)", "sbx-1", "user1", LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO ibpms_sandbox_instances (id, created_by, created_at) VALUES (?, ?, ?)", "sbx-2", "user1", LocalDateTime.now());
        jdbcTemplate.update("INSERT INTO ibpms_sandbox_instances (id, created_by, created_at) VALUES (?, ?, ?)", "sbx-3", "user1", LocalDateTime.now());

        // Attempt 4th
        given()
            .header("X-Mock-User", "user1")
            .contentType(ContentType.JSON)
            .body("{\"processKey\": \"test-model\"}")
        .when()
            .post("/start")
        .then()
            .statusCode(429); // Too Many Requests
    }

    @Test
    @DisplayName("CA-67: testSandboxAutoDestroyAfter10Minutes")
    void testSandboxAutoDestroyAfter10Minutes() {
        // Insert expired instance (15 mins old)
        jdbcTemplate.update("INSERT INTO ibpms_sandbox_instances (id, created_by, created_at) VALUES (?, ?, ?)", 
                            "sbx-expired", "user1", LocalDateTime.now().minusMinutes(15));
        
        // Manual validation trigger
        given()
        .when()
            .post("/system/cleanup")
        .then()
            .statusCode(200);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_sandbox_instances WHERE id = 'sbx-expired'", Integer.class);
        assertEquals(0, count, "Las instancias con más de 10 minutos de antigüedad deben destruirse");
    }
}
