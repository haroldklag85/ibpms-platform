package com.ibpms.poc.integration.kanban;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanBoardEntity;
import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import com.ibpms.poc.infrastructure.jpa.entity.TimeLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanBoardRepository;
import com.ibpms.poc.infrastructure.jpa.repository.KanbanTaskRepository;
import com.ibpms.poc.infrastructure.jpa.repository.TimeLogRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeTrackingIT {

    @LocalServerPort
    private int port;

    @Autowired
    private KanbanTaskRepository taskRepository;

    @Autowired
    private KanbanBoardRepository boardRepository;

    @Autowired
    private TimeLogRepository timeLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String validToken;
    private KanbanTaskEntity task;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        validToken = jwtTokenProvider.generateToken(
                "user_supervisor", Collections.singletonList("ROLE_SUPERVISOR"), "tenant1");

        timeLogRepository.deleteAll();
        taskRepository.deleteAll();
        boardRepository.deleteAll();

        KanbanBoardEntity board = new KanbanBoardEntity();
        board.setProjectName("Board IT");
        board.setOwnerId("tenant1");
        board = boardRepository.save(board);

        task = new KanbanTaskEntity();
        task.setBoard(board);
        task.setTitle("Task IT");
        task.setStatus("IN_PROGRESS");
        task = taskRepository.save(task);
    }

    @Test
    void testStartTimerInProgress() {
        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("referenceId", task.getId().toString(), "referenceType", "TASK_AGILE"))
        .when()
            .post("/api/v1/time-tracking/start")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    void testStartTimerInTodo() {
        task.setStatus("TODO");
        taskRepository.save(task);

        given()
            .header("Authorization", "Bearer " + validToken)
            .contentType(ContentType.JSON)
            .body(Map.of("referenceId", task.getId().toString(), "referenceType", "TASK_AGILE"))
        .when()
            .post("/api/v1/time-tracking/start")
        .then()
            .statusCode(400);
    }

    @Test
    void testStopTimer() {
        TimeLogEntity log = new TimeLogEntity();
        log.setId(UUID.randomUUID());
        log.setReferenceId(task.getId());
        log.setReferenceType("TASK_AGILE");
        log.setUserId("user_supervisor");
        log.setStartedAt(ZonedDateTime.now().minusMinutes(10));
        log.setCreatedAt(ZonedDateTime.now());
        log = timeLogRepository.save(log);

        given()
            .header("Authorization", "Bearer " + validToken)
        .when()
            .post("/api/v1/time-tracking/stop/" + log.getId())
        .then()
            .statusCode(200)
            .body("durationMinutes", notNullValue());
    }

    @Test
    void testDeleteNotAllowed() {
        given()
            .header("Authorization", "Bearer " + validToken)
        .when()
            .delete("/api/v1/time-tracking/delete/something")
        .then()
            .statusCode(405);
    }
}
