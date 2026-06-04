// @Traceability: US-005, CA-41 - ADR-001
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-005, CA-67, CA-41 (Reemplazo DDL mock por Liquibase Testcontainer)
@Traceability(US = "US-005", CA = {"CA-67", "CA-41"})
public class SandboxGovernanceTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_SANDBOX_COUNTER_KEY = "sandbox_active_simulations";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/processes";
        redisTemplate.delete(REDIS_SANDBOX_COUNTER_KEY);
    }

    @Test
    @DisplayName("CA-67: testMaxThreeConcurrentSandboxInstances")
    void testMaxThreeConcurrentSandboxInstances() {
        // Set up the limit to exactly 3 manually
        redisTemplate.opsForValue().set(REDIS_SANDBOX_COUNTER_KEY, "3");

        byte[] smallFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:definitions></bpmn:definitions>".getBytes();

        // Attempt 4th using the simulate endpoint
        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
            .multiPart("file", "small-process.xml", smallFile, "application/xml")
        .when()
            .post("/sandbox-simulate")
        .then()
            .statusCode(429); // Too Many Requests (ResourceExhaustedException)
            
        // Ensure limit remains
        String count = redisTemplate.opsForValue().get(REDIS_SANDBOX_COUNTER_KEY);
        assertEquals("3", count, "El contador de sandbox en Redis no debió aumentar tras el rechazo");
    }

    @Test
    @DisplayName("CA-67: testSandboxAutoDestroyAfter10Minutes")
    void testSandboxAutoDestroyAfter10Minutes() {
        // Instead of testing a manual cron/system cleanup, we test that SandboxInterceptor sets a TTL
        redisTemplate.delete(REDIS_SANDBOX_COUNTER_KEY);

        byte[] smallFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:definitions></bpmn:definitions>".getBytes();

        // 1 request should succeed and initialize counter
        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
            .multiPart("file", "small-process.xml", smallFile, "application/xml")
        .when()
            .post("/sandbox-simulate")
        .then()
            .statusCode(200);

        Long expire = redisTemplate.getExpire(REDIS_SANDBOX_COUNTER_KEY);
        org.junit.jupiter.api.Assertions.assertTrue(expire != null && expire > 0, "Redis key must have an expiration TTL");
    }

    @Test
    @DisplayName("CA-41: testSandboxSpawnEndpointSuccess")
    void testSandboxSpawnEndpointSuccess() {
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:process id=\"test-process-key\" isExecutable=\"true\"></bpmn:process>";

        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
            .contentType(ContentType.JSON)
            .body(Map.of("xml", testXml))
        .when()
            .post("/sandbox-spawn")
        .then()
            .statusCode(200)
            .body("status", equalTo("SIMULATION_DESTROYED"));
    }

    @Test
    @DisplayName("CA-41: testSandboxSpawnEndpointMissingHeader")
    void testSandboxSpawnEndpointMissingHeader() {
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bpmn:process id=\"test-process-key\" isExecutable=\"true\"></bpmn:process>";

        given()
            .header("X-Mock-User", "user1")
            .contentType(ContentType.JSON)
            .body(Map.of("xml", testXml))
        .when()
            .post("/sandbox-spawn")
        .then()
            .statusCode(409); // Conflict (IllegalStateException mapped in GlobalExceptionHandler)
    }
}
