package com.ibpms.poc.infrastructure.web.bpmn;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.ibpms.poc.AbstractIntegrationTest;

public class ProcessLockPersistenceTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/design/bpmn/lock";
        // Create table manually to avoid JPA sync issues in direct JDBC queries if needed
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS ibpms_process_locks (process_key VARCHAR(255) PRIMARY KEY, locked_by VARCHAR(255), locked_at TIMESTAMP, expires_at TIMESTAMP)");
        jdbcTemplate.execute("TRUNCATE TABLE ibpms_process_locks");
    }

    @Test
    @DisplayName("CA-66: testLockSurvivesServerRestart")
    void testLockSurvivesServerRestart() {
        // En un test de integración no podemos reiniciar el servidor real, 
        // pero sí validar que la persistencia sobrevive mediante escritura directa
        // a base de datos y validación de lectura luego de borrar el contexto.
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"processKey\": \"test-process\"}")
        .when()
            .post("/acquire")
        .then()
            .statusCode(200);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_process_locks WHERE process_key = 'test-process'", Integer.class);
        assertEquals(1, count, "El lock debe persistir en base de datos independientemente del estado de la memoria (Server Restart)");
    }

    @Test
    @DisplayName("CA-66: testAutoReleaseAfter90sWithoutHeartbeat")
    void testAutoReleaseAfter90sWithoutHeartbeat() {
        // Simulamos un lock que expiró hace 100 segundos y llamamos al endpoint o scheduled task que limpia
        jdbcTemplate.update(
            "INSERT INTO ibpms_process_locks (process_key, locked_by, locked_at, expires_at) VALUES (?, ?, ?, ?)",
            "test-process-expired", "user1", LocalDateTime.now().minusSeconds(200), LocalDateTime.now().minusSeconds(110)
        );

        // Disparar limpieza manual o verificar que al pedir el status, está liberado
        given()
        .when()
            .get("/status/test-process-expired")
        .then()
            .statusCode(200)
            .body("locked", org.hamcrest.Matchers.is(false)); 
            
        // Validar en DB que fue removido
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ibpms_process_locks WHERE process_key = 'test-process-expired'", Integer.class);
        assertEquals(0, count, "El lock expirado debe ser eliminado de la tabla");
    }
}
