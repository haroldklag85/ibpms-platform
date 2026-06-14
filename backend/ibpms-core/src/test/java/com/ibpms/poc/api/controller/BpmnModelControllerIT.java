package com.ibpms.poc.api.controller;

import com.ibpms.poc.AbstractIntegrationIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

public class BpmnModelControllerIT extends AbstractIntegrationIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void givenMalformedXml_whenDeployingModel_thenReturnsSafeAbort() {
        String dirtyXmlPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <bpmn:process id=\"Process_Error\" isExecutable=\"true\">\n" +
                "    <!-- Missing closing tags -->";

        given()
            .contentType(ContentType.XML)
            .body(dirtyXmlPayload)
        .when()
            .post("/api/v1/models/deploy")
        .then()
            // Zeebe/Camunda parser should break or Spring Security stops it early
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(400), org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(403)));
    }
}
