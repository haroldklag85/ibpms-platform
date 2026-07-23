// @Traceability: US-007 - ADR-001
package com.ibpms.poc.integration;

import com.ibpms.poc.AbstractIntegrationIT;

import com.ibpms.poc.domain.model.agile.AgileProject;
import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.persistence.AgileProjectRepositoryJpa;
import com.ibpms.poc.infrastructure.persistence.AgileTaskRepositoryJpa;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class ClaimIntegrationIT extends AbstractIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private AgileTaskRepositoryJpa repository;

    @Autowired
    private AgileProjectRepositoryJpa projectRepository;

    @Autowired
    private com.ibpms.poc.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    private String validToken;
    private String supervisorToken;
    private UUID testProjectId;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        repository.deleteAll(); // Limpiar
        
        AgileProject project = new AgileProject();
        project.setName("QA Agile Project");
        project.setDescription("Project for Claim IT");
        project.setMethodology("KANBAN_CONTINUOUS");
        project.setStatus("ACTIVE");
        project.setCreatedBy("admin");
        project.setCreatedAt(ZonedDateTime.now());
        AgileProject savedProject = projectRepository.save(project);
        testProjectId = savedProject.getId();

        validToken = jwtTokenProvider.generateToken("user1", java.util.Collections.singletonList("ROLE_OPERARIO"), "tenantA");
        supervisorToken = jwtTokenProvider.generateToken("super1", java.util.Collections.singletonList("ROLE_SUPERVISOR"), "tenantA");
    }

    @Test
    public void testConcurrentClaim() throws InterruptedException {
        AgileTask task = new AgileTask();
        task.setProjectId(testProjectId);
        task.setTitle("Task 1");
        task.setStatus("AVAILABLE");
        task.setCreatedBy("test_user");
        task = repository.save(task);

        UUID taskId = task.getId();
        
        // Simular 2 peticiones concurrentes (Thread 1 - user1, Thread 2 - user2)
        // En una prueba unitaria pura es difícil, lo haremos secuencial aquí para probar el endpoint.
        
        // 1. user1 hace claim -> 200
        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/tasks/{taskId}/claim", taskId)
        .then()
            .statusCode(200);
            
        // 2. user2 hace claim (simulando token user2) -> 409
        String user2Token = jwtTokenProvider.generateToken("user2", java.util.Collections.singletonList("ROLE_OPERARIO"), "tenantA");
        given()
            .header("Authorization", "Bearer " + user2Token)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/tasks/{taskId}/claim", taskId)
        .then()
            .statusCode(409);
    }

    @Test
    public void testForceUnclaimWithoutTeamMatch() {
        AgileTask task = new AgileTask();
        task.setProjectId(testProjectId);
        task.setTitle("Task 2");
        task.setStatus("CLAIMED");
        task.setTeamId("team-alpha"); // El supervisorToken es team-beta
        task.setAssigneeIds(new java.util.HashSet<>(java.util.Collections.singletonList("user1")));
        task.setCreatedBy("test_user");
        task = repository.save(task);

        given()
            .header("Authorization", "Bearer " + supervisorToken)
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/tasks/{taskId}/force-unclaim", task.getId())
        .then()
            .statusCode(403)
            .body("detail", containsString("No tiene permisos"));
    }

    @Test
    public void testReleaseWithMessage() {
        AgileTask task = new AgileTask();
        task.setProjectId(testProjectId);
        task.setTitle("Task 3");
        task.setStatus("CLAIMED");
        task.setAssigneeIds(new java.util.HashSet<>(java.util.Collections.singletonList("user1")));
        task.setCreatedBy("test_user");
        task = repository.save(task);

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("message", "Devuelvo la tarea por falta de datos"))
        .when()
            .post("/api/v1/tasks/{taskId}/unclaim", task.getId())
        .then()
            .statusCode(200);
    }
}
