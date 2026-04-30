package com.ibpms.poc.api.controller;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

public class SecurityIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void givenUnauthenticatedRequest_whenAccessingWorkdesk_thenReturns401() {
        given()
            .log().all()
        .when()
            .get("/workdesk/global-inbox")
        .then()
            .log().all()
            // Si la ruta requiere auth y no se provee, el ApplicationFilterChain dispara HTTP 401 o 403
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(403)));
    }

    @Test
    public void givenInvalidToken_whenAccessingWorkdesk_thenReturns401() {
        given()
            .header("Authorization", "Bearer tokeninvalido.estructurarota.completamente")
            .log().all()
        .when()
            .get("/workdesk/global-inbox")
        .then()
            .log().all()
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(403)));
    }
}
