// @Traceability: US-005, CA-65
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.springframework.boot.test.web.server.LocalServerPort;


public class BpmnDeployContractTest extends AbstractIntegrationTest {

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
    @DisplayName("CA-65: testDeployWithValidCommentReturns201")
    void testDeployWithValidCommentReturns201() {
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <camunda:property name=\"ReglaNomenclatura\" value=\"CASO-${GENERIC}\" />\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"sys_generic_form\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(validBpmn.getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Comentario de despliegue válido")
        .when()
            .post("/deploy")
        .then()
            .statusCode(201)
            .body("deployment_id", notNullValue())
            .body("version", notNullValue())
            .body("deployed_at", notNullValue())
            .body("deployed_by", notNullValue());
    }

    @Test
    @DisplayName("CA-65: testDeployWithoutCommentReturns400")
    void testDeployWithoutCommentReturns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder("<bpmn/>".getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testDeployCommentTooShortReturns400")
    void testDeployCommentTooShortReturns400() {
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder("<bpmn/>".getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Corta") // 5 chars
        .when()
            .post("/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: testValidateEndpointDoesNotDeploy")
    void testValidateEndpointDoesNotDeploy() {
        String validBpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <camunda:property name=\"ReglaNomenclatura\" value=\"CASO-${GENERIC}\" />\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" camunda:formKey=\"sys_generic_form\" />\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(validBpmn.getBytes())
                    .controlName("file")
                    .fileName("dummy.bpmn")
                    .mimeType("application/xml")
                    .build())
        .when()
            .post("/validate")
        .then()
            .statusCode(200)
            .body("deployment_id", nullValue())
            .body("errors", notNullValue());
    }

    @Test
    @DisplayName("CA-65: testDeployFileSizeExceeds5MBReturns413")
    void testDeployFileSizeExceeds5MBReturns413() {
        byte[] largeFile = new byte[6 * 1024 * 1024]; // 6MB
        given()
            .header("Authorization", "Bearer " + token)
            .multiPart(new io.restassured.builder.MultiPartSpecBuilder(largeFile)
                    .controlName("file")
                    .fileName("large.bpmn")
                    .mimeType("application/xml")
                    .build())
            .multiPart("deploy_comment", "Deploy archivo extremadamente grande")
        .when()
            .post("/deploy")
        .then()
            .statusCode(413);
    }
}
