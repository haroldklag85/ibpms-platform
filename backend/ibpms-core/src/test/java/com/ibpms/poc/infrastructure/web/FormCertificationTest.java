package com.ibpms.poc.infrastructure.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Integration tests for FormCertification (US-028 CA-11/CA-12/CA-13/CA-15/CA-16/CA-17).
 * Runs against PostgreSQL via Testcontainers (inherited from AbstractIntegrationTest).
 * Compliant with ADR-010: No external Docker Compose dependency.
 */
@Traceability(US = "US-003", CA = {"CA-87"})
public class FormCertificationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @org.springframework.beans.factory.annotation.Autowired
    private com.ibpms.poc.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        // @Traceability(US="US-003", CA="CA-87", DESC="Generate token with required JIT claims to avoid 403 Forbidden")
        token = jwtTokenProvider.generateToken(
            "test-qa-user",
            java.util.List.of("ibpms_rol_USER"),
            "default",
            java.util.Map.of("Sucursal_ID", "SUC-001", "Codigo_Jefe", "BOSS-999")
        );
    }

    @Test
    public void testCertifyFormSuccessfully() {
        // CA-11: Certificar formulario exitosamente
        UUID formId = UUID.randomUUID();
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .post("/api/v1/design/forms/{id}/certify", formId)
            .then()
            .statusCode(200)
            .body("is_qa_certified", equalTo(true));
    }

    @Test
    public void testRevokeSealOnSchemaMutation() {
        // CA-12: Revocar sello al modificar esquema
        // Step 1: Create and certify a form
        UUID formId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .post("/api/v1/design/forms/{id}/certify", formId)
            .then()
            .statusCode(200)
            .body("is_qa_certified", equalTo(true));

        // Step 2: Modify the schema via the form-definitions endpoint
        String mutatedPayload = "{\"schema\": { \"field1\": \"string\", \"mutated\": true } }";
        given()
            .contentType(ContentType.JSON)
            .body(mutatedPayload)
            .when()
            .post("/api/v1/design/form-definitions/{formId}", formId)
            .then()
            .statusCode(200)
            .body("is_qa_certified", equalTo(false));
    }

    @Test
    public void testNewVersionCreatedWithoutSeal() {
        // CA-13: Nueva versión nace sin sello
        UUID formId = UUID.randomUUID();
        
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/design/forms/{id}/versions", formId)
            .then()
            .statusCode(201)
            .body("is_qa_certified", equalTo(false));
    }

    @Test
    public void testLargePayloadIsCompressed() {
        // CA-15: Payload >32KB se comprime (guardado y auditoría)
        UUID formId = UUID.randomUUID();
        // Generar un string grande de 40KB
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40000; i++) {
            sb.append("A");
        }
        String payload = "{\"schema\": { \"largeField\": \"" + sb.toString() + "\" } }";

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/design/form-definitions/{formId}", formId)
            .then()
            .statusCode(200);
    }

    @Test
    public void testConcurrencyReturns409() {
        // CA-16: Concurrencia: segundo intento → 409
        UUID formId = UUID.randomUUID();

        // Primer intento
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .post("/api/v1/design/forms/{id}/certify", formId)
            .then()
            .statusCode(200);

        // Segundo intento inmediato deberia dar 409
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .post("/api/v1/design/forms/{id}/certify", formId)
            .then()
            .statusCode(409)
            .body("error", containsString("ya fue certificado"));
    }

    @Test
    public void testBpmnVariablesEndpointReturnsList() {
        // CA-17: Endpoint variables BPMN retorna lista
        String processKey = "test-process-key";
        
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/design/processes/{processKey}/variables", processKey)
            .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class));
    }

    @Test
    public void testCertifyWithoutAuthReturnsUnauthorized() {
        // POST /certify sin Authorization header -> HTTP 401 o 403
        UUID formId = UUID.randomUUID();
        
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/design/forms/{id}/certify", formId)
            .then()
            .statusCode(anyOf(is(401), is(403)));
    }
}
