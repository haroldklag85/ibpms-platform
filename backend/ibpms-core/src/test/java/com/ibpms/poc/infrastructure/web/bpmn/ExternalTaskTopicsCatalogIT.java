// @Traceability: US-005, CA-70
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationIT;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.springframework.boot.test.web.server.LocalServerPort;


public class ExternalTaskTopicsCatalogIT extends AbstractIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        this.token = jwtTokenProvider.generateToken("BPMN_Release_Manager", java.util.Arrays.asList("ibpms_rol_BPMN_Release_Manager"), "tenant_alpha");
    }

    @Test
    @DisplayName("CA-70: testGetTopicsReturnsSeedData")
    void testGetTopicsReturnsSeedData() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/topics")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(6));
    }

    @Test
    @DisplayName("CA-70: testPreFlightRejectsUnknownTopic")
    void testPreFlightRejectsUnknownTopic() {
        // Enviar BPMN que contiene <camunda:task topic="inventado"/> para fallar en la validación
        String bpmnContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:serviceTask id=\"Task_1\" camunda:type=\"external\" camunda:topic=\"inventado\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(bpmnContent.getBytes())
                    .controlName("file")
                    .fileName("test.bpmn")
                    .mimeType("application/xml")
                    .build())
        .when()
            .post("/validate")
        .then()
            .statusCode(200) // Endpoint returns 200 with list of errors
            .body("errors.message", hasItem(containsString("inventado")));
    }
}
