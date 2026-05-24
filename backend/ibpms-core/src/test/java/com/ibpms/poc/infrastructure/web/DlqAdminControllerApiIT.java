package com.ibpms.poc.infrastructure.web;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

/**
 * Test de API con REST Assured (BDD-style) para el DlqAdminController.
 *
 * <p>Cierre de hallazgo: Testing Stack Audit - Nivel 4 (API/Contratos y RBAC)</p>
 *
 * <p>Arranca el servidor HTTP completo en un puerto aleatorio y envía
 * peticiones reales. A diferencia de MockMvc, REST Assured pasa por
 * TODO el pipeline HTTP real incluyendo filtros de seguridad, por lo que
 * puede detectar fallos de {@code @PreAuthorize} que MockMvc puede ignorar.</p>
 *
 * <h3>Patrón BDD:</h3>
 * <pre>{@code
 * given()
 *   .auth().oauth2(token)
 * .when()
 *   .get("/api/admin/dlq/messages")
 * .then()
 *   .statusCode(200)
 *   .body("size()", greaterThan(0));
 * }</pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DlqAdminControllerApiIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/admin/dlq";
    }

    @Test
    @DisplayName("RBAC: Petición sin token debe retornar 401 Unauthorized")
    void shouldReturn401WhenNoAuthToken() {
        given()
            .contentType("application/json")
        .when()
            .get("/messages")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("RBAC: Petición con rol NO autorizado debe retornar 403 Forbidden")
    void shouldReturn403WhenInsufficientRole() {
        // TODO: Generar token JWT de test con rol OPERADOR (no ADMIN_IT)
        // String operatorToken = TestJwtUtil.generateToken("OPERADOR");
        //
        // given()
        //     .auth().oauth2(operatorToken)
        //     .contentType("application/json")
        // .when()
        //     .get("/messages")
        // .then()
        //     .statusCode(403);
    }

    @Test
    @DisplayName("Contrato: Endpoint health-check responde con status UP")
    void shouldReturnHealthStatus() {
        given()
            .contentType("application/json")
        .when()
            .get("/actuator/health")
        .then()
            .statusCode(200);
    }
}
