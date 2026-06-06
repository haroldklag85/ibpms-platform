// @Traceability: US-007 - ADR-001
package com.ibpms.poc.integration.kanban;

import com.ibpms.poc.AbstractIntegrationTest;


import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KanbanStateTransitionIT extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private KanbanTaskRepository taskRepository;

    @Autowired
    private KanbanBoardRepository boardRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String validToken;
    private KanbanTaskEntity task;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        validToken = jwtTokenProvider.generateToken(
                "user_supervisor", Collections.singletonList("ROLE_SUPERVISOR"), "tenant1");

        taskRepository.deleteAll();
        boardRepository.deleteAll();

        KanbanBoardEntity board = new KanbanBoardEntity();
        board.setProjectName("Board IT");
        board.setOwnerId("tenant1");
        board = boardRepository.save(board);

        task = new KanbanTaskEntity();
        task.setBoard(board);
        task.setTitle("Task IT");
        task.setStatus("TODO");
        task = taskRepository.save(task);
    }

    @Test
    void testPatchStateTodoToInProgress() {
        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("newState", "IN_PROGRESS"))
        .when()
            .patch("/api/v1/kanban/" + task.getId() + "/state")
        .then()
            .statusCode(200);

        KanbanTaskEntity updated = taskRepository.findById(task.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("IN_PROGRESS", updated.getStatus());
    }

    @Test
    void testPatchStateDoneToTodo() {
        task.setStatus("DONE");
        taskRepository.save(task);

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("newState", "TODO"))
        .when()
            .patch("/api/v1/kanban/" + task.getId() + "/state")
        .then()
            .statusCode(400);
    }

    @Test
    void testPatchStateBlockedWithoutReason() {
        task.setStatus("IN_PROGRESS");
        taskRepository.save(task);

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("newState", "BLOCKED", "blockedReason", ""))
        .when()
            .patch("/api/v1/kanban/" + task.getId() + "/state")
        .then()
            .statusCode(400);
    }
}
