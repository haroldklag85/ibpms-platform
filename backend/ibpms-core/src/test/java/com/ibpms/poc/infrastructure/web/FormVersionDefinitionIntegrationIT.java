package com.ibpms.poc.infrastructure.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.ibpms.poc.AbstractIntegrationIT;
import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Integration tests for versioned persistence and collision check of visual form design (CA-87).
 * Extends AbstractIntegrationIT to use test database configuration.
 *
 * @Traceability: US-003 - CA-87
 */
@Traceability(US = "US-003", CA = {"CA-87"})
public class FormVersionDefinitionIntegrationIT extends AbstractIntegrationIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testFormVersionLifecycleAndCollision() {
        // Generar un ID unico de formulario
        UUID formId = UUID.randomUUID();
        String technicalName = "test-form-ca87-" + formId.toString().substring(0, 8);

        // 1. Crear el formulario base (versión 1 en ibpms_form_design)
        String payloadV1 = """
            {
                "name": "Formulario CA87 V1",
                "technicalName": "%s",
                "pattern": "SIMPLE",
                "vueTemplate": "<template><input id='name'/></template>",
                "zodSchema": "z.object({ name: z.string() })",
                "formFields": [
                    {
                        "camundaVariable": "customerName",
                        "type": "text",
                        "zodRule": "z.string()"
                    }
                ]
            }
            """.formatted(technicalName);

        // Guardamos el diseño inicial vía POST a /api/v1/forms (creación)
        // Usamos el ID generado como formId
        String createdFormId = given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "architect")
            .body(payloadV1)
            .when()
            .post("/api/v1/forms")
            .then()
            .statusCode(201)
            .body("technicalName", equalTo(technicalName))
            .extract()
            .path("id");

        UUID finalFormId = UUID.fromString(createdFormId);

        // Persistimos en ibpms_form_definitions vía el POST /api/v1/forms/{formId}
        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "architect")
            .body(payloadV1)
            .when()
            .post("/api/v1/forms/{id}", finalFormId)
            .then()
            .statusCode(200)
            .body("version", equalTo(1));

        // 2. Modificamos el diseño para crear una nueva versión 2
        String payloadV2 = """
            {
                "name": "Formulario CA87 V2",
                "technicalName": "%s",
                "pattern": "SIMPLE",
                "vueTemplate": "<template><input id='name'/><input id='age'/></template>",
                "zodSchema": "z.object({ name: z.string(), age: z.number() })",
                "formFields": [
                    {
                        "camundaVariable": "customerName",
                        "type": "text",
                        "zodRule": "z.string()"
                    },
                    {
                        "camundaVariable": "customerAge",
                        "type": "number",
                        "zodRule": "z.number()"
                    }
                ]
            }
            """.formatted(technicalName);

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "architect")
            .body(payloadV2)
            .when()
            .post("/api/v1/forms/{id}", finalFormId)
            .then()
            .statusCode(200)
            .body("version", equalTo(2));

        // 3. Volvemos a enviar exactamente el mismo contenido para simular colisión
        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "architect")
            .body(payloadV2)
            .when()
            .post("/api/v1/forms/{id}", finalFormId)
            .then()
            .statusCode(409)
            .body("error", containsString("collision"));

        // 4. Listar las versiones persistidas en ibpms_form_definitions
        given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/forms/{id}/versions", finalFormId)
            .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].version", equalTo(2))
            .body("[1].version", equalTo(1));
    }
}
