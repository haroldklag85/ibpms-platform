package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-005, CA-66 (Reemplazo DDL mock por Liquibase Testcontainer)
@Traceability(US = "US-005", CA = {"CA-66"})
public class ProcessLockPersistenceTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_process_locks CASCADE");
    }

    @Test
    @DisplayName("CA-66: testLockSurvivesServerRestart")
    void testLockSurvivesServerRestart() {
        given()
            .header("X-Mock-User", "user1")
            .contentType(ContentType.JSON)
            .body("{\"processKey\": \"test-process\"}")
        .when()
            .post("/test-process/lock")
        .then()
            .statusCode(200);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_process_locks WHERE process_definition_key = 'test-process'", Integer.class);
        assertEquals(1, count, "El lock debe persistir en base de datos independientemente del estado de la memoria (Server Restart)");
    }

    @Test
    @DisplayName("CA-66: testAutoReleaseAfter90sWithoutHeartbeat")
    void testAutoReleaseAfter90sWithoutHeartbeat() {
        jdbcTemplate.update(
            "INSERT INTO ibpms_process_locks (process_definition_key, locked_by, locked_at, browser_session_id) VALUES (?, ?, ?, ?)",
            "test-process-expired", "user1", LocalDateTime.now().minusSeconds(200), "session123"
        );

        given()
        .header("X-Mock-User", "user2")
        .when()
            .post("/test-process-expired/lock")
        .then()
            .statusCode(200); 
            
        String lockedBy = jdbcTemplate.queryForObject(
                "SELECT locked_by FROM ibpms_process_locks WHERE process_definition_key = 'test-process-expired'", String.class);
        assertEquals("user2", lockedBy, "El lock expirado debe ser transferido a user2");
    }
}
