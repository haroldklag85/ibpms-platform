package com.ibpms.poc.infrastructure.web.security;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class GenericFormIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ibpms_test")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    // ==========================================
    // CA-4: Validación del Cuerpo Editable
    // ==========================================

    @Test
    @DisplayName("CA-4: POST /generic-form-complete con observations < 5 chars -> HTTP 400")
    void testCa4_ShortObservationsShouldFail() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("observations", "ok"); // < 5 chars
        payload.put("managementResult", "APPROVED");

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/generic-form-complete")
        .then()
            .statusCode(400); // TDD Fail-Fast: Expected 400
    }

    @Test
    @DisplayName("CA-4: POST /generic-form-complete con >5 attachmentUuids -> HTTP 400")
    void testCa4_TooManyAttachmentsShouldFail() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("observations", "Observations valid length");
        payload.put("managementResult", "APPROVED");
        payload.put("attachmentUuids", Arrays.asList("id1", "id2", "id3", "id4", "id5", "id6"));

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/generic-form-complete")
        .then()
            .statusCode(400); // Límite de adjuntos según remediación
    }

    @Test
    @DisplayName("CA-4: POST /generic-form-complete válido -> HTTP 200")
    void testCa4_ValidPayloadShouldPass() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("observations", "Todo en orden, procede."); // > 5 chars
        payload.put("managementResult", "APPROVED");
        payload.put("attachmentUuids", Arrays.asList("doc-1234"));

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/generic-form-complete")
        .then()
            .statusCode(200); 
    }

    // ==========================================
    // CA-5: Whitelist Regex por Proceso
    // ==========================================

    @Test
    @DisplayName("CA-5: El sistema debe sanitizar prefillData descartando variables_internal")
    void testCa5_InternalVariablesDiscarded() {
        // Asumiendo que el GET details invoca al BFF y retorna la variables.
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/tasks/mock-task-id-123/details")
        .then()
            // Even if the endpoint doesn't exist, we document the contract
            .statusCode(200)
            .body("prefillData._internal_var", nullValue());
    }

    // ==========================================
    // CA-7: Persistencia y Auto-Guardado
    // ==========================================

    @Test
    @DisplayName("CA-7: PUT a /drafts/{taskId} persistencia idempotente")
    void testCa7_DraftAutosave() {
        Map<String, Object> draft = new HashMap<>();
        draft.put("observations", "Parcialmente escrito");

        given()
            .contentType(ContentType.JSON)
            .body(draft)
        .when()
            .put("/drafts/t-100")
        .then()
            // The existing dummy returns 200 GET, but PUT isn't fully implemented in proxy
            // If it fails with 405/404, it tells us the implementation is missing
            .statusCode(204); 
    }

    // ==========================================
    // CA-8: Botones de Pánico
    // ==========================================

    @Test
    @DisplayName("CA-8: POST /generic-form-complete con CANCELLED sin justificación -> HTTP 400")
    void testCa8_CancelledRequiresJustification() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("panicAction", "CANCELLED"); // trigger panic
        // Sin justificación

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/generic-form-complete")
        .then()
            .statusCode(400); 
    }
}
