package com.ibpms.poc.api.controller;

import com.ibpms.poc.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskClaimControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        // Limpiar caché de redis antes de cada prueba para aislar estado
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    public void testTaskClaim_Success() {
        String taskId = "task-uuid-1234";

        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/tasks/{taskId}/claim", taskId)
        .then()
            .statusCode(200);

        // Verificar que el candado quedó en Redis
        String lockVal = redisTemplate.opsForValue().get("lock:task:claim:" + taskId);
        assertThat(lockVal).isEqualTo("e2e_user");
    }

    @Test
    public void testTaskClaim_RaceConditionPrevention() throws InterruptedException {
        String taskId = "task-uuid-concurrent";
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(() -> {
                Response response = given()
                        .contentType(ContentType.JSON)
                        .post("/api/v1/tasks/{taskId}/claim", taskId);
                return response.statusCode();
            });
        }

        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        
        int successCount = 0;
        int conflictCount = 0;

        for (Future<Integer> future : futures) {
            try {
                int status = future.get();
                if (status == 200) successCount++;
                if (status == 409) conflictCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        // Asegurarse de que el candado atómico SETNX realmente hizo su trabajo
        assertThat(successCount).as("Solo un hilo debe ganar el candado").isEqualTo(1);
        assertThat(conflictCount).as("El resto de hilos deben ser rechazados").isEqualTo(numberOfThreads - 1);
    }
}
