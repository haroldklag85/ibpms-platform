package com.ibpms.poc;

import com.ibpms.poc.domain.model.TaskDraft;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
public class RateLimitDraftTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskDraftRepository taskDraftRepository;

    @BeforeEach
    void setUp() {
        taskDraftRepository.deleteAll();
    }

    @Test
    void testRateLimitingOnDrafts() {
        String taskId = "task-rate-limit";
        
        // Simular draft
        TaskDraft draft = TaskDraft.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId("test-user")
                .partialData("{\"A\":\"B\"}")
                .schemaVersion("V1")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
        taskDraftRepository.save(draft);

        // Agotar los 6 tokens iniciales (con usuario simulado si fuera mvc, pero restTemplate no pasa mock_user auto)
        // Ya que esto es un IntegrationTest extenso, podemos testearlo con Mvc local:
        // Pero usaremos la interfaz directa del Bean para probar Bucket4j si restTemplate causa 401.
    }
}
