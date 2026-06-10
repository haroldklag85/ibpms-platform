package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Traceability(US = "US-005", CA = {"CA-68"})
public class DataMappingIntegrityTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        jdbcTemplate.execute("DELETE FROM ibpms_data_mappings WHERE process_definition_key = 'test-process'");
    }


    @Test
    @DisplayName("CA-68: testSaveMappingPersistsInDatabase")
    void testSaveMappingPersistsInDatabase() throws Exception {
        // Payload compatible con BpmnDesignController.createDataMapping
        String mappingPayload = "{\"taskId\":\"Task_1\", \"connectorId\":\"conn-01\", \"mappingJson\":\"{\\\"bpmnVar\\\":\\\"monto\\\"}\"}";
        
        given()
            .contentType(ContentType.JSON)
            .body(mappingPayload)
        .when()
            .post("/api/v1/design/processes/test-process/data-mappings")
        .then()
            .statusCode(201);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_data_mappings WHERE process_definition_key = 'test-process'", Integer.class);
        assertEquals(1, count, "El mapeo debe ser persistido en la base de datos");
    }
}
