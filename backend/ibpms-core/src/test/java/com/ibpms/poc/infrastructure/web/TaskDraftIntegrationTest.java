package com.ibpms.poc.infrastructure.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;
import java.util.Map;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;

/**
 * Integration tests for CA-91 (CQRS Integration Contracts: drafts auto-save, recovery, complete, and clean-up).
 * Extends AbstractIntegrationTest to use test database configuration.
 *
 * @Traceability: US-003 - CA-91
 */
@Traceability(US = "US-003", CA = {"CA-91"})
public class TaskDraftIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AgileTaskRepositoryJpa agileTaskRepositoryJpa;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        // Generate JWT token with necessary JIT claims
        token = jwtTokenProvider.generateToken(
            "test-qa-user",
            List.of("ibpms_rol_USER"),
            "default",
            Map.of("Sucursal_ID", "SUC-001", "Codigo_Jefe", "BOSS-999")
        );
    }

    @Test
    public void testTaskDraftLifecycle() {
        // Create an AgileTask in CLAIMED state to be able to save draft
        AgileTask task = AgileTask.builder()
            .projectId(UUID.fromString("50000000-0000-0000-0000-000000000001"))
            .title("Task for CA91 Integration test")
            .status("CLAIMED")
            .position(1)
            .createdBy("test-qa-user")
            .build();

        task = agileTaskRepositoryJpa.save(task);
        UUID taskId = task.getId();

        // 1. Save draft payload via POST /api/v1/drafts/{taskId}
        String draftPayload = """
            {
                "customerName": "John Doe",
                "customerAge": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(draftPayload)
            .when()
            .post("/api/v1/drafts/{taskId}", taskId)
            .then()
            .statusCode(200);

        // 2. Retrieve draft payload via GET /api/v1/drafts/{taskId}
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/v1/drafts/{taskId}", taskId)
            .then()
            .statusCode(200)
            .body("customerName", equalTo("John Doe"))
            .body("customerAge", equalTo(30));

        // 3. Delete draft payload via DELETE /api/v1/drafts/{taskId}
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/api/v1/drafts/{taskId}", taskId)
            .then()
            .statusCode(204);

        // 4. Retrieve draft after deletion - should return empty JSON map
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/v1/drafts/{taskId}", taskId)
            .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    public void testCompleteTaskContract() {
        // Create another AgileTask in CLAIMED state
        AgileTask task = AgileTask.builder()
            .projectId(UUID.fromString("50000000-0000-0000-0000-000000000001"))
            .title("Task for CA91 Complete test")
            .status("CLAIMED")
            .position(2)
            .createdBy("test-qa-user")
            .build();

        task = agileTaskRepositoryJpa.save(task);
        UUID taskId = task.getId();

        // Complete task via POST /api/v1/tasks/{taskId}/complete
        String completePayload = """
            {
                "variables": {
                    "approved": true,
                    "comments": "Looks good"
                }
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(completePayload)
            .when()
            .post("/api/v1/tasks/{taskId}/complete", taskId)
            .then()
            .statusCode(204);

        // Verify task status is updated to COMPLETED in the database
        AgileTask updatedTask = agileTaskRepositoryJpa.findById(taskId)
            .orElseThrow(() -> new AssertionError("Task not found"));
        assertEquals("COMPLETED", updatedTask.getStatus());
    }

    @Test
    public void testCompleteTaskConflict() {
        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-72: Pruebas de conflicto de bloqueo optimista
        // Create another AgileTask in CLAIMED state
        AgileTask task = AgileTask.builder()
            .projectId(UUID.fromString("50000000-0000-0000-0000-000000000001"))
            .title("Task for CA72 Optimistic Lock Conflict test")
            .status("CLAIMED")
            .position(3)
            .createdBy("test-qa-user")
            .build();

        task = agileTaskRepositoryJpa.save(task);
        UUID taskId = task.getId();

        // Compute the initial optimistic hash (outdated version)
        String outdatedVersionId = org.springframework.util.DigestUtils.md5DigestAsHex(
            (task.getId() + "_" + task.getStatus() + "_" + task.getUpdatedAt().toInstant().toEpochMilli()).getBytes()
        );

        // Modify the task to change the updatedAt timestamp (simulating a concurrent update)
        task.setUpdatedAt(task.getUpdatedAt().plusSeconds(10));
        task = agileTaskRepositoryJpa.save(task);

        String completePayload = """
            {
                "variables": {
                    "approved": true,
                    "comments": "Outdated completion try"
                }
            }
            """;

        // Try completing with the outdated If-Match header -> should return 409 Conflict
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .header("If-Match", outdatedVersionId)
            .body(completePayload)
            .when()
            .post("/api/v1/tasks/{taskId}/complete", taskId)
            .then()
            .statusCode(409);

        // Compute the new current optimistic hash (valid version)
        String currentVersionId = org.springframework.util.DigestUtils.md5DigestAsHex(
            (task.getId() + "_" + task.getStatus() + "_" + task.getUpdatedAt().toInstant().toEpochMilli()).getBytes()
        );

        // Try completing with the correct If-Match header -> should return 204 No Content
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .header("If-Match", currentVersionId)
            .body(completePayload)
            .when()
            .post("/api/v1/tasks/{taskId}/complete", taskId)
            .then()
            .statusCode(204);

        // Verify task status is updated to COMPLETED in the database
        AgileTask updatedTask = agileTaskRepositoryJpa.findById(taskId)
            .orElseThrow(() -> new AssertionError("Task not found"));
        assertEquals("COMPLETED", updatedTask.getStatus());
    }
}
