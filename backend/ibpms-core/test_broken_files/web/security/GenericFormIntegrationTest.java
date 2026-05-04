package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.AbstractIntegrationTest;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.springframework.boot.test.web.server.LocalServerPort;


public class GenericFormIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

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
            .statusCode(204); 
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

    @Test
    @DisplayName("CA-5: PUT /generic-form-config con >10 claves en whitelist -> HTTP 400")
    void testCa5_WhitelistExceeds10ShouldFail() {
        List<String> tooManyKeys = java.util.stream.IntStream.range(0, 11)
            .mapToObj(i -> "var_" + i)
            .collect(java.util.stream.Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("whitelist", tooManyKeys);

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/design/processes/test-process/generic-form-config")
        .then()
            .statusCode(400);
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

    // ==========================================
    // QA-TEST-01: VIP Pre-Flight Restrictor
    // ==========================================

    @Test
    @DisplayName("QA-TEST-01: VIP dinámico - Insertar rol, verificar bloqueo sin re-deploy")
    void testQa01_DynamicVipRestrictor() {
        given()
            .header("Authorization", "Bearer VIP_USER_TOKEN")
            .contentType(ContentType.JSON)
        .when()
            // Simulamos abrir tarea de un formKey restringido
            .get("/workbox/tasks/mock-task-id/details")
        .then()
            // Se espera HTTP 403 con 'RESTRICCIÓN VIP'
            .statusCode(403);
    }

    // ==========================================
    // QA-TEST-02: Segregación de Funciones (SoD)
    // ==========================================

    @Test
    @DisplayName("QA-TEST-02: SoD - Initiator no puede auto-aprobar")
    void testQa02_SegregationOfDutiesRestriction() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("observations", "Intentando auto-completar 12345");
        payload.put("managementResult", "APPROVED");

        given()
            .header("Authorization", "Bearer INITIATOR_TOKEN")
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/workbox/tasks/mock-task-id/generic-form-complete")
        .then()
            .statusCode(403);
    }

    // ==========================================
    // QA-TEST-03: Whitelist Configurable
    // ==========================================

    @Test
    @DisplayName("QA-TEST-03: Configuracion de Whitelist via PUT")
    void testQa03_WhitelistConfiguration() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("whitelist", Arrays.asList("Case_ID", "amount", "priority"));

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .put("/design/processes/test-process/generic-form-config")
        .then()
            // HTTP 200 al tener < 10 variables válidas
            .statusCode(200);
            
        // Se esperaria que GET devuelva solo lo configurado:
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/generic-form-context")
        .then()
            .statusCode(200);
    }
}
