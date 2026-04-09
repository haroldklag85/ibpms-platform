package com.ibpms.poc.infrastructure.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class WorkdeskRepositoryTest {

    @Autowired
    private WorkdeskProjectionRepository workdeskRepository;

    // ============================================================
    // TEST 1 (76-DEV / CA-14): Tenant Isolation + Impact Sorting
    // ============================================================
    @Test
    void testFindWorkdeskTasks_TenantIsolationAndSorting() {
        // Arrange
        WorkdeskProjectionEntity task1 = new WorkdeskProjectionEntity();
        task1.setId("task1");
        task1.setTenantId("tenantA");
        task1.setImpactLevel(10);
        task1.setTitle("High Impact Task");
        task1.setSourceSystem("BPMN");
        task1.setOriginalTaskId("t1");
        task1.setStatus("ACTIVE");
        task1.setSlaExpirationDate(LocalDateTime.now().plusDays(2));

        WorkdeskProjectionEntity task2 = new WorkdeskProjectionEntity();
        task2.setId("task2");
        task2.setTenantId("tenantA");
        task2.setImpactLevel(5);
        task2.setTitle("Low Impact Task");
        task2.setSourceSystem("BPMN");
        task2.setOriginalTaskId("t2");
        task2.setStatus("ACTIVE");
        task2.setSlaExpirationDate(LocalDateTime.now().plusDays(1));

        WorkdeskProjectionEntity task3 = new WorkdeskProjectionEntity();
        task3.setId("task3");
        task3.setTenantId("tenantB");
        task3.setImpactLevel(100);
        task3.setTitle("Tenant B Task");
        task3.setSourceSystem("BPMN");
        task3.setOriginalTaskId("t3");
        task3.setStatus("ACTIVE");

        workdeskRepository.save(task1);
        workdeskRepository.save(task2);
        workdeskRepository.save(task3);

        // Act — CA-14: Isolation test for tenantA (should return 2, ignore task3)
        // CA-17: Sorting test (task1 should be first because impactLevel 10 > 5)
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantA", null, null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("task1", result.getContent().get(0).getId()); // Highest impact
        assertEquals("task2", result.getContent().get(1).getId());
    }

    // ============================================================
    // TEST 2 (77-DEV / CA-17): NULLS LAST SLA ordering
    // ============================================================
    @Test
    void testFindWorkdeskTasks_SlaAscNullsLast() {
        // Arrange — Two tasks with same impact; one with SLA, one without
        WorkdeskProjectionEntity taskWithSla = new WorkdeskProjectionEntity();
        taskWithSla.setId("sla_yes");
        taskWithSla.setTenantId("tenantNL");
        taskWithSla.setImpactLevel(5);
        taskWithSla.setTitle("Task With SLA");
        taskWithSla.setSourceSystem("BPMN");
        taskWithSla.setOriginalTaskId("nl1");
        taskWithSla.setStatus("ACTIVE");
        taskWithSla.setSlaExpirationDate(LocalDateTime.now().plusDays(1));

        WorkdeskProjectionEntity taskNoSla = new WorkdeskProjectionEntity();
        taskNoSla.setId("sla_no");
        taskNoSla.setTenantId("tenantNL");
        taskNoSla.setImpactLevel(5); // Same impact → SLA sort decides
        taskNoSla.setTitle("Task Without SLA");
        taskNoSla.setSourceSystem("KANBAN");
        taskNoSla.setOriginalTaskId("nl2");
        taskNoSla.setStatus("ACTIVE");
        taskNoSla.setSlaExpirationDate(null); // NULL SLA

        workdeskRepository.save(taskWithSla);
        workdeskRepository.save(taskNoSla);

        // Act — CA-17: sla_expiration_date ASC NULLS LAST
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantNL", null, null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        // Task with SLA should come FIRST; null SLA goes LAST
        assertEquals("sla_yes", result.getContent().get(0).getId());
        assertEquals("sla_no", result.getContent().get(1).getId());
        assertNull(result.getContent().get(1).getSlaExpirationDate());
    }

    // ============================================================
    // TEST 3 (76-DEV / CA-19): ILIKE partial search
    // ============================================================
    @Test
    void testFindWorkdeskTasks_FuzzySearchTrgm() {
        // Arrange
        WorkdeskProjectionEntity task1 = new WorkdeskProjectionEntity();
        task1.setId("task_search");
        task1.setTenantId("tenantA");
        task1.setImpactLevel(1);
        task1.setTitle("Urgent approval request");
        task1.setSourceSystem("BPMN");
        task1.setOriginalTaskId("ts1");
        task1.setStatus("ACTIVE");

        workdeskRepository.save(task1);

        // Act — CA-10 / CA-19: ILike partial search (case-insensitive)
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantA", "ApPrOvAl", null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("task_search", result.getContent().get(0).getId());
    }

    // ============================================================
    // TEST 4 (77-DEV / CA-23): progressPercent present
    // ============================================================
    @Test
    void testFindWorkdeskTasks_ProgressPercentPresent() {
        // Arrange — entity with progressPercent = 60
        WorkdeskProjectionEntity task = new WorkdeskProjectionEntity();
        task.setId("progress_yes");
        task.setTenantId("tenantProg");
        task.setImpactLevel(3);
        task.setTitle("Task With Progress");
        task.setSourceSystem("BPMN");
        task.setOriginalTaskId("pg1");
        task.setStatus("ACTIVE");
        task.setProgressPercent(60);
        task.setTotalSteps(5);
        task.setCurrentStep(3);

        workdeskRepository.save(task);

        // Act
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantProg", null, null, PageRequest.of(0, 10));

        // Assert — CA-23: progressPercent should be 60
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        WorkdeskProjectionEntity loaded = result.getContent().get(0);
        assertEquals(60, loaded.getProgressPercent());
        assertEquals(5, loaded.getTotalSteps());
        assertEquals(3, loaded.getCurrentStep());
    }

    // ============================================================
    // TEST 5 (77-DEV / CA-23): progressPercent null (N/D fallback)
    // ============================================================
    @Test
    void testFindWorkdeskTasks_ProgressPercentNull() {
        // Arrange — entity with progressPercent = null (non-linear BPMN / unknown)
        WorkdeskProjectionEntity task = new WorkdeskProjectionEntity();
        task.setId("progress_nd");
        task.setTenantId("tenantProgNull");
        task.setImpactLevel(2);
        task.setTitle("Task Without Progress");
        task.setSourceSystem("KANBAN");
        task.setOriginalTaskId("pg2");
        task.setStatus("ACTIVE");
        // progressPercent deliberately NOT set → defaults to null

        workdeskRepository.save(task);

        // Act
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantProgNull", null, null, PageRequest.of(0, 10));

        // Assert — CA-23: progressPercent should be null → Frontend renders "N/D"
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        WorkdeskProjectionEntity loaded = result.getContent().get(0);
        assertNull(loaded.getProgressPercent());
        assertNull(loaded.getTotalSteps());
        assertNull(loaded.getCurrentStep());
    }

    // ============================================================
    // TEST 6 (Fase 2 - CA-22/CA-29): Faceted Filters and Tenant Isolation
    // ============================================================
    @Test
    void testCountByStatusPerTenant() {
        // Arrange
        WorkdeskProjectionEntity task1 = new WorkdeskProjectionEntity();
        task1.setId("f1");
        task1.setTitle("T1");
        task1.setTenantId("tenantF");
        task1.setImpactLevel(1);
        task1.setStatus("ACTIVE");
        task1.setSourceSystem("BPMN");
        task1.setOriginalTaskId("t1");

        WorkdeskProjectionEntity task2 = new WorkdeskProjectionEntity();
        task2.setId("f2");
        task2.setTitle("T2");
        task2.setTenantId("tenantF");
        task2.setImpactLevel(2);
        task2.setStatus("ACTIVE");
        task2.setSourceSystem("BPMN");
        task2.setOriginalTaskId("t2");

        WorkdeskProjectionEntity task3 = new WorkdeskProjectionEntity();
        task3.setId("f3");
        task3.setTitle("T3");
        task3.setTenantId("tenantF");
        task3.setImpactLevel(3);
        task3.setStatus("COMPLETED");
        task3.setSourceSystem("BPMN");
        task3.setOriginalTaskId("t3");

        WorkdeskProjectionEntity task4 = new WorkdeskProjectionEntity();
        task4.setId("f4");
        task4.setTitle("T4");
        task4.setTenantId("tenantOther");
        task4.setImpactLevel(4);
        task4.setStatus("DRAFT");
        task4.setSourceSystem("BPMN");
        task4.setOriginalTaskId("t4");

        workdeskRepository.save(task1);
        workdeskRepository.save(task2);
        workdeskRepository.save(task3);
        workdeskRepository.save(task4);

        // Act
        java.util.List<com.ibpms.poc.application.dto.FacetCountDto> facets = workdeskRepository.countByStatusPerTenant("tenantF");

        // Assert
        assertNotNull(facets);
        assertEquals(2, facets.size()); // Should only have ACTIVE and COMPLETED for tenantF
        
        long activeCount = facets.stream().filter(f -> "ACTIVE".equals(f.getStatus())).findFirst().get().getCount();
        long completedCount = facets.stream().filter(f -> "COMPLETED".equals(f.getStatus())).findFirst().get().getCount();
        
        assertEquals(2L, activeCount);
        assertEquals(1L, completedCount);
    }
}
