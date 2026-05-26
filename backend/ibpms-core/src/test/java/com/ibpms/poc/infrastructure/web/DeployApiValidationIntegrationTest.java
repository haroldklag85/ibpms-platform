package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@DisplayName("Pruebas de Integración - Validación de API Deploy (CA-65)")
public class DeployApiValidationIntegrationTest extends AbstractIntegrationTest {

    // Al mockear la cadena de filtros, RestAssured podrá acceder directo al Controller sin 401
    @MockBean
    private SecurityFilterChain securityFilterChain;

    private static final String DEPLOY_URL = "/api/v1/design/processes/deploy";

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("CA-65: Debe arrojar 400 Bad Request si el archivo está vacío")
    void testDeploy_WithEmptyFile_ShouldReturn400() {
        given()
            .multiPart(new MultiPartSpecBuilder(new byte[0])
                    .controlName("file")
                    .fileName("proceso.bpmn")
                    .mimeType("application/xml")
                    .build())
            .formParam("deploy_comment", "Comentario valido")
            .header("X-Sandbox-Mode", "true")
        .when()
            .post(DEPLOY_URL)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65: Debe arrojar 415 Unsupported Media Type al enviar application/json")
    void testDeploy_WithJsonPayload_ShouldReturn415() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"file\":\"dummy.bpmn\", \"deploy_comment\":\"Comentario valido\"}")
            .header("X-Sandbox-Mode", "true")
        .when()
            .post(DEPLOY_URL)
        .then()
            .statusCode(415);
    }

    @Test
    @DisplayName("CA-65: Debe arrojar 400 o 415 al enviar un archivo con extensión inválida (text/plain)")
    void testDeploy_WithInvalidTextFile_ShouldReturn400or415() {
        int statusCode = given()
            .multiPart(new MultiPartSpecBuilder("contenido de texto simple".getBytes())
                    .controlName("file")
                    .fileName("dummy.txt")
                    .mimeType(ContentType.TEXT.toString())
                    .build())
            .formParam("deploy_comment", "Comentario valido")
            .header("X-Sandbox-Mode", "true")
        .when()
            .post(DEPLOY_URL)
        .thenReturn().statusCode();

        org.junit.jupiter.api.Assertions.assertTrue(statusCode == 400 || statusCode == 415,
                "El código de estado debe ser 400 o 415, se obtuvo: " + statusCode);
    }
}
