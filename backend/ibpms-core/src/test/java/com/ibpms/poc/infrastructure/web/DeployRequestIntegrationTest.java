package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

@DisplayName("CA-34 y CA-69: Solicitud de Despliegue")
public class DeployRequestIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        try {
            jdbcTemplate.execute("DELETE FROM ibpms_bpmn_process_design WHERE technical_id = 'Process_DeployReq'");
            jdbcTemplate.execute("INSERT INTO ibpms_security_user (id, username, email, status, is_external_idp, created_at, must_change_password) VALUES (gen_random_uuid(), 'admin', 'admin@local.com', 'ACTIVE', false, CURRENT_TIMESTAMP, false) ON CONFLICT DO NOTHING");
            jdbcTemplate.execute("INSERT INTO ibpms_bpmn_process_design (id, technical_id, name, form_pattern, status, current_version, max_nodes, created_at, updated_at, created_by, is_public) " +
                    "VALUES (gen_random_uuid(), 'Process_DeployReq', 'Test', 'SIMPLE', 'DRAFT', 1, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', false) ON CONFLICT DO NOTHING");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getValidToken() {
        return jwtTokenProvider.generateToken("admin", java.util.Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "T1");
    }

    // @Traceability: US-005, CA-34
    @Test
    @DisplayName("Debe crear una solicitud de despliegue y devolver 201 Created")
    void shouldCreateDeployRequest() {
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn2:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn2:process id=\"Process_DeployReq\" isExecutable=\"true\">\n" +
                "    <bpmn2:startEvent id=\"StartEvent_1\" />\n" +
                "  </bpmn2:process>\n" +
                "</bpmn2:definitions>";

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .multiPart("file", "test.bpmn", bpmnContent.getBytes(), "text/xml")
        .when()
            .post("/api/v1/design/processes/deploy-request")
        .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value());
    }

    // @Traceability: US-005, CA-34
    @Test
    @DisplayName("Debe rechazar la revision con comentario muy corto devolviendo 400 Bad Request")
    void shouldRejectReviewWithShortComment() {
        String jsonBody = "{\"approved\": false, \"comment\": \"corto\"}";

        given()
            .header("Authorization", "Bearer " + getValidToken())
            .contentType(ContentType.JSON)
            .body(jsonBody)
        .when()
            .post("/api/v1/design/processes/deploy-requests/123e4567-e89b-12d3-a456-426614174000/review")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
