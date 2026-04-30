package com.ibpms.poc;

import com.ibpms.poc.domain.model.TaskDraft;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class TaskDraftCrudTest extends AbstractIntegrationTest {

    @Autowired
    private TaskDraftRepository taskDraftRepository;

    @Test
    void testTaskDraftCrudCycle() {
        String taskId = "test-task-123";
        String userId = "test-user-456";

        // 1. GET returns empty
        Optional<TaskDraft> notFound = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        assertTrue(notFound.isEmpty());

        // 2. PUT creates
        TaskDraft draft = TaskDraft.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .currentStep(1)
                .partialData("{\"foo\": \"bar\"}")
                .schemaVersion("V2")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
        TaskDraft saved = taskDraftRepository.save(draft);
        assertNotNull(saved.getId());

        // 3. GET retrieves
        Optional<TaskDraft> found = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        assertTrue(found.isPresent());
        assertEquals("{\"foo\": \"bar\"}", found.get().getPartialData());

        // 4. PUT updates existing
        TaskDraft toUpdate = found.get();
        toUpdate.setPartialData("{\"foo\": \"baz\"}");
        taskDraftRepository.save(toUpdate);

        Optional<TaskDraft> updated = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        assertTrue(updated.isPresent());
        assertEquals("{\"foo\": \"baz\"}", updated.get().getPartialData());

        // 5. DELETE removes
        taskDraftRepository.deleteById(updated.get().getId());
        Optional<TaskDraft> deleted = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        assertTrue(deleted.isEmpty());
    }
}
