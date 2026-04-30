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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ibpms.poc.AbstractIntegrationTest;

public class DataMappingIntegrityTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;
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
