// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.AbstractIntegrationTest;


import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class WorkdeskRLSTest extends AbstractIntegrationTest {

    @Autowired
    private WorkdeskProjectionRepository repository;

    @Test
    @WithMockUser(username = "userA")
    public void shouldFilterTasksByAssignee_RLS_UserA() {
        // Setup: One task for userA, one for userB, one unassigned
        createTask("task1", "userA");
        createTask("task2", "userB");
        createTask("task3", null);

        // Execute: Find all (Aspect should inject filter)
        List<WorkdeskProjectionEntity> tasks = repository.findAll();

        // Verify: Should see userA task AND unassigned task (pool), but NOT userB task
        assertThat(tasks).anyMatch(t -> "userA".equals(t.getAssignee()));
        assertThat(tasks).anyMatch(t -> t.getAssignee() == null);
        assertThat(tasks).noneMatch(t -> "userB".equals(t.getAssignee()));
    }

    @Test
    @WithMockUser(username = "userB")
    public void shouldFilterTasksByAssignee_RLS_UserB() {
        createTask("task1", "userA");
        createTask("task2", "userB");
        createTask("task3", null);

        List<WorkdeskProjectionEntity> tasks = repository.findAll();

        assertThat(tasks).anyMatch(t -> "userB".equals(t.getAssignee()));
        assertThat(tasks).anyMatch(t -> t.getAssignee() == null);
        assertThat(tasks).noneMatch(t -> "userA".equals(t.getAssignee()));
    }

    private void createTask(String id, String assignee) {
        WorkdeskProjectionEntity entity = new WorkdeskProjectionEntity();
        entity.setId(id);
        entity.setSourceSystem("BPMN");
        entity.setOriginalTaskId(id);
        entity.setTitle("Task " + id);
        entity.setAssignee(assignee);
        entity.setTenantId("tenant1");
        entity.setStatus("OPEN");
        entity.setImpactLevel(5);
        repository.save(entity);
    }
}
