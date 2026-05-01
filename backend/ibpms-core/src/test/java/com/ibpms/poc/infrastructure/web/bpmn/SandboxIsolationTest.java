package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class SandboxIsolationTest extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/sandbox";
    }

    @Test
    @DisplayName("CA-63: testSandboxModePreventsRealApiCalls")
    void testSandboxModePreventsRealApiCalls() {
        // Enviar un payload que dispararía una tarea externa real, simulando que estamos en modo Sandbox
        given()
            .header("X-Sandbox-Mode", "true")
            .contentType(ContentType.JSON)
            .body("{\"payload\": {\"amount\": 5000}}")
        .when()
            .post("/workers/execute-mock")
        .then()
            .statusCode(200)
            .body("status", equalTo("mocked"))
            .body("real_api_called", equalTo(false));
    }
}
