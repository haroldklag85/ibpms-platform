package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.agile.AgileTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ibpms.poc.AbstractIntegrationTest;

@Transactional
public class AgileTaskRepositoryJpaTest extends AbstractIntegrationTest {

    @Autowired
    private AgileTaskRepositoryJpa agileTaskRepositoryJpa;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @org.junit.jupiter.api.BeforeEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM ibpms_agile_tasks");
    }

    @Test
    void should_find_next_available_task_with_skip_locked() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_agile_projects (id, name, created_by) VALUES (?, ?, ?)", projectId, "Test Project", "user1");

        AgileTask task1 = AgileTask.builder()
                .projectId(projectId)
                .title("Task 1")
                .status("AVAILABLE")
                .createdBy("user1")
                .slaDeadline(java.time.ZonedDateTime.now().plusDays(1))
                .build();
        task1 = agileTaskRepositoryJpa.save(task1);

        AgileTask task2 = AgileTask.builder()
                .projectId(projectId)
                .title("Task 2")
                .status("IN_PROGRESS")
                .createdBy("user1")
                .slaDeadline(java.time.ZonedDateTime.now().plusDays(2))
                .build();
        task2 = agileTaskRepositoryJpa.save(task2);

        entityManager.flush();

        // Act
        Optional<AgileTask> nextAvailable = agileTaskRepositoryJpa.findNextAvailableTaskForUpdate();

        // Assert
        assertTrue(nextAvailable.isPresent(), "Should find an available task");
        assertEquals(task1.getId(), nextAvailable.get().getId(), "Should be task1 since it's AVAILABLE and earlier SLA");
    }

    @Test
    void should_find_by_id_for_update_with_skip_locked() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ibpms_agile_projects (id, name, created_by) VALUES (?, ?, ?)", projectId, "Test Project", "user1");

        AgileTask task = AgileTask.builder()
                .projectId(projectId)
                .title("Task Locked")
                .status("AVAILABLE")
                .createdBy("user2")
                .slaDeadline(java.time.ZonedDateTime.now().plusDays(1))
                .build();
        task = agileTaskRepositoryJpa.save(task);
        entityManager.flush();

        // Act
        Optional<AgileTask> lockedTask = agileTaskRepositoryJpa.findByIdForUpdate(task.getId());

        // Assert
        assertTrue(lockedTask.isPresent(), "Should lock and retrieve the task");
        assertEquals(task.getTitle(), lockedTask.get().getTitle());
    }
}
