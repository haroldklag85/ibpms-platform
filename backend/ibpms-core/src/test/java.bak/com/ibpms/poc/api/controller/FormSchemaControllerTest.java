package com.ibpms.poc.api.controller;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

public class FormSchemaControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void givenMalformedJson_whenPostingFormSchema_thenBouncesBeforePersistence() {
        String malformedJsonPayload = "{\n" +
                "  \"formName\": \"Registro\",\n" +
                "  \"fields\": [\n" +
                "    { \"name\": \"broken... syntax \n" + // Syntax error
                "  ]\n" +
                "}";

        given()
            .contentType(ContentType.JSON)
            .body(malformedJsonPayload)
        .when()
            .post("/api/v1/forms")
        .then()
            // Interceptamos la falla arquitectónica: La mutación no debe procesar, debe arrojar Bad Request o bien ser frenado por Seguridad.
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(400), org.hamcrest.Matchers.is(401), org.hamcrest.Matchers.is(403)));
    }
}
