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
public class DataMappingIntegrityTest {

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
        RestAssured.basePath = "/api/v1/design/mappings";
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ibpms_data_mappings (id VARCHAR(255) PRIMARY KEY, process_key VARCHAR(255), form_id VARCHAR(255), mapping_json TEXT)");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_data_mappings");
    }

    @Test
    @DisplayName("CA-68: testSaveMappingPersistsInDatabase")
    void testSaveMappingPersistsInDatabase() {
        String mappingPayload = "{\"processKey\":\"test-process\", \"formId\":\"formx1\", \"mappings\":[{\"bpmnVar\":\"monto\", \"zodField\":\"monto_solicitado\"}] }";
        
        given()
            .contentType(ContentType.JSON)
            .body(mappingPayload)
        .when()
            .post("/")
        .then()
            .statusCode(201);
            
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_data_mappings WHERE process_key = 'test-process'", Integer.class);
        assertEquals(1, count, "El mapeo debe ser persistido en la base de datos");
    }

    @Test
    @DisplayName("CA-68: testPreFlightDetectsOrphanedMapping")
    void testPreFlightDetectsOrphanedMapping() {
        String mappingPayload = "{\"processKey\":\"test-orphaned\", \"formId\":\"formx2\", \"mappings\":[{\"bpmnVar\":\"deleted_var\", \"zodField\":\"monto_solicitado\"}] }";
        // Pre-flight check before deployment to detect orphaned relations
        given()
            .contentType(ContentType.JSON)
            .body(mappingPayload)
        .when()
            .post("/pre-flight")
        .then()
            .statusCode(200)
            .body("errors", hasItem(containsString("deleted_var")));
    }
}
