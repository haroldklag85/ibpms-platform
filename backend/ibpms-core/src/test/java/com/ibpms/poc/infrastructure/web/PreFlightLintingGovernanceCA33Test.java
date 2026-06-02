package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@DisplayName("CA-33: Linting Governance")
public class PreFlightLintingGovernanceCA33Test extends AbstractIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    // @Traceability: US-005, CA-33 Reglas de Linting del Pre-Flight
    @Test
    @DisplayName("Debe bloquear despliegue con warnings incluso si force_deploy=true")
    void shouldBlockDeploymentOnWarningsEvenWithForceDeploy() {
        // Arrange
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_CA33\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                "    <bpmn:intermediateCatchEvent id=\"Event_1\">\n" +
                "      <bpmn:messageEventDefinition id=\"MessageEventDef_1\"/>\n" +
                "    </bpmn:intermediateCatchEvent>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        // Generar un JWT valido firmado con la clave real del perfil de test usando JwtTokenProvider
        String token = jwtTokenProvider.generateToken("BPMN_Release_Manager", java.util.Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "tenant_alpha");

        // Act & Assert
        given()
            .header("Authorization", "Bearer " + token)
            .header("X-Sandbox-Mode", "true")
            .multiPart("file", "process_ca33.bpmn", bpmnContent.getBytes(), "text/xml")
            .param("deploy_comment", "Comentario de validacion de CA-33 de mas de diez chars")
            .param("force_deploy", "true")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            // Currently this test will FAIL (TDD Red Phase) because the endpoint returns 201 Created due to force_deploy=true.
            // We expect it to be 422 Unprocessable Entity
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body("error", containsString("Hard-Stop (CA-33)"));
    }
}
