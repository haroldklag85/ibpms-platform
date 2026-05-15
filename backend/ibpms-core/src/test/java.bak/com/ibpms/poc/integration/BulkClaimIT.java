package com.ibpms.poc.integration;

import com.ibpms.poc.domain.model.agile.AgileTask;
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

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class BulkClaimIT {

    @LocalServerPort
    private int port;

    @Autowired
    private AgileTaskRepositoryJpa repository;

    @Autowired
    private com.ibpms.poc.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    private String validToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        repository.deleteAll();
        
        validToken = jwtTokenProvider.generateToken("user1", Collections.singletonList("ROLE_OPERARIO"), "tenantA");
    }

    @Test
    public void testBulkClaimMixed() {
        List<String> taskIds = new ArrayList<>();
        // 3 Tareas disponibles
        for (int i = 0; i < 3; i++) {
            AgileTask t = new AgileTask();
            t.setProjectId(UUID.randomUUID());
            t.setTitle("AvTask " + i);
            t.setStatus("AVAILABLE");
            taskIds.add(repository.save(t).getId().toString());
        }
        // 2 Tareas ocupadas (conflicto)
        for (int i = 0; i < 2; i++) {
            AgileTask t = new AgileTask();
            t.setProjectId(UUID.randomUUID());
            t.setTitle("OccTask " + i);
            t.setStatus("CLAIMED");
            taskIds.add(repository.save(t).getId().toString());
        }

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("taskIds", taskIds))
        .when()
            .post("/api/v1/tasks/bulk-claim")
        .then()
            .statusCode(200)
            .body("claimed.size()", equalTo(3))
            .body("conflicts.size()", equalTo(2));
    }

    @Test
    public void testBulkClaimLimitExceeded() {
        List<String> taskIds = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            taskIds.add(UUID.randomUUID().toString());
        }

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("taskIds", taskIds))
        .when()
            .post("/api/v1/tasks/bulk-claim")
        .then()
            .statusCode(400)
            .body("error", containsString("Hard limit of 20 tasks exceeded"));
    }
}
