package com.ibpms.poc.api.controller;

import com.ibpms.poc.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
public class TaskClaimControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // Limpiar el Redis para asegurar no contaminación entre tests
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void claimTask_ShouldProvideMutualExclusionUnderHighConcurrency() throws InterruptedException {
        // Arrange
        String taskId = "task-concurrent-999";
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger conflictCounter = new AtomicInteger(0);

        // Act - Simulate 10 simultaneous requests to the same endpoint
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    MvcResult result = mockMvc.perform(post("/api/v1/tasks/{taskId}/claim", taskId))
                                              .andReturn();
                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCounter.incrementAndGet();
                    } else if (status == 409) {
                        conflictCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Assert
        // Expect EXACTLY 1 successful claim, and EXACTLY N-1 conflicts due to Redis SETNX Distributed Lock
        assertThat(successCounter.get()).as("Debe haber exactamente un solo reclamo exitoso").isEqualTo(1);
        assertThat(conflictCounter.get()).as("Deben haber 9 intentos rechazados por candado ocupado").isEqualTo(numberOfThreads - 1);
        
        // Verifica que la llave en Redis realmente exista y contenga el valor de e2e_user (mock)
        String lockValue = redisTemplate.opsForValue().get("lock:task:claim:" + taskId);
        assertThat(lockValue).isEqualTo("e2e_user");
    }

    @Test
    void unclaimTask_ShouldReleaseLockAndSucceed() throws Exception {
        // Arrange
        String taskId = "task-to-unclaim";
        mockMvc.perform(post("/api/v1/tasks/{taskId}/claim", taskId))
               .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));

        // Verificar que exista
        assertThat(redisTemplate.hasKey("lock:task:claim:" + taskId)).isTrue();

        // Act
        mockMvc.perform(post("/api/v1/tasks/{taskId}/unclaim", taskId))
               .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(200));

        // Assert - Redis Lock must be gone
        assertThat(redisTemplate.hasKey("lock:task:claim:" + taskId)).isFalse();
    }
}
