// @Traceability: US-005, CA-41 - ADR-001
package com.ibpms.poc.infrastructure.web.bpmn;

import com.ibpms.poc.AbstractIntegrationIT;

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
public class SandboxGovernanceIT extends AbstractIntegrationIT {

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
        java.util.Set<String> keys = redisTemplate.keys("sandbox_rate_limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
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

    @Test
    @DisplayName("CA-82: testSandboxSpawnEndpointMissingVariableReturns422")
    void testSandboxSpawnEndpointMissingVariableReturns422() {
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Def_1\">\n" +
                "  <bpmn:process id=\"process-test-var\" isExecutable=\"true\">\n" +
                "    <bpmn:sequenceFlow id=\"flow1\" sourceRef=\"start\" targetRef=\"task\">\n" +
                "      <bpmn:conditionExpression>${monto > 50000}</bpmn:conditionExpression>\n" +
                "    </bpmn:sequenceFlow>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
            .contentType(ContentType.JSON)
            .body(Map.of("xml", testXml))
        .when()
            .post("/sandbox-spawn")
        .then()
            .statusCode(422)
            .body("error", equalTo("MISSING_VARIABLE"))
            .body("variableName", equalTo("monto"));
    }

    @Test
    @DisplayName("CA-82: testSandboxSpawnEndpointWithVariablesSuccess")
    void testSandboxSpawnEndpointWithVariablesSuccess() {
        String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"Def_1\">\n" +
                "  <bpmn:process id=\"process-test-var\" isExecutable=\"true\">\n" +
                "    <bpmn:sequenceFlow id=\"flow1\" sourceRef=\"start\" targetRef=\"task\">\n" +
                "      <bpmn:conditionExpression>${monto > 50000}</bpmn:conditionExpression>\n" +
                "    </bpmn:sequenceFlow>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
            .contentType(ContentType.JSON)
            .body(Map.of(
                "xml", testXml,
                "variables", Map.of("monto", 60000)
            ))
        .when()
            .post("/sandbox-spawn")
        .then()
            .statusCode(200)
            .body("status", equalTo("SIMULATION_DESTROYED"));
    }

    @Test
    @DisplayName("US-005: testGetProcessVersionsNotFoundReturnsEmptyList")
    void testGetProcessVersionsNotFoundReturnsEmptyList() {
        given()
            .header("X-Mock-User", "user1")
            .header("X-Sandbox-Mode", "true")
        .when()
            .get("/non-existent-process-key/versions")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }
}
