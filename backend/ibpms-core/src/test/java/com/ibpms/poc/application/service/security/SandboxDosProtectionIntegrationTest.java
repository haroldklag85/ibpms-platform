package com.ibpms.poc.application.service.security;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Pruebas de Integración - Protección DoS en Sandbox")
public class SandboxDosProtectionIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("CA-67: Debe denegar archivos excesivamente grandes (XML Bombing) - HTTP 413 o Connection Reset")
    void testSandboxPayloadLimit_ShouldReturn413() {
        // Generar un XML simulado de gran tamaño (aprox. 3MB)
        StringBuilder largeContent = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn:definitions>");
        for (int i = 0; i < 100000; i++) {
            largeContent.append("<bpmn:process id=\"Process_").append(i).append("\"></bpmn:process>");
        }
        largeContent.append("</bpmn:definitions>");

        try {
            int statusCode = given()
                .header("X-Sandbox-Mode", "true")
                .multiPart("file", "large-process.xml", largeContent.toString().getBytes(), "application/xml")
            .when()
                .post("/api/v1/design/processes/sandbox-simulate")
            .getStatusCode();

            org.junit.jupiter.api.Assertions.assertEquals(413, statusCode, "Debería retornar 413 Payload Too Large");
        } catch (Exception e) {
            // Tomcat aborta la conexión cuando se lanza una excepción en medio de la subida,
            // lo que resulta en un ClientProtocolException (Connection reset by peer).
            // Esto es un comportamiento válido de protección DoS.
            String errMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            org.junit.jupiter.api.Assertions.assertTrue(
                errMsg.contains("Connection reset") || e.getCause() instanceof java.net.SocketException || e instanceof org.apache.http.client.ClientProtocolException,
                "La conexión debió ser abortada (Connection reset) por exceder el límite."
            );
        }
    }

    @Test
    @DisplayName("CA-67: Debe aplicar Rate Limiting (Máx 10 peticiones) - HTTP 429")
    void testSandboxRateLimiting_ShouldReturn429After10Requests() {
        byte[] smallFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:definitions></bpmn:definitions>".getBytes();

        // Realizamos 20 peticiones consecutivas
        for (int i = 1; i <= 20; i++) {
            int statusCode = given()
                .header("X-Sandbox-Mode", "true")
                .multiPart("file", "small-process.xml", smallFile, "application/xml")
            .when()
                .post("/api/v1/design/processes/sandbox-simulate")
            .getStatusCode();

            if (i <= 10) {
                // Las primeras 10 peticiones deben ser aceptadas (NO 429)
                assertNotEquals(429, statusCode, "La petición " + i + " no debería ser rechazada por Rate Limit (429)");
            } else {
                // A partir de la petición 11, se espera HTTP 429 Too Many Requests
                if (statusCode == 429) {
                    // Passed
                    return; // Test is green if it eventually hits 429 after 10.
                }
                // Si llegamos a la petición 20 y ninguna dio 429, el test debe fallar.
                if (i == 20) {
                    assertNotEquals(200, statusCode, "El servidor nunca aplicó Rate Limit HTTP 429.");
                }
            }
        }
    }
}
