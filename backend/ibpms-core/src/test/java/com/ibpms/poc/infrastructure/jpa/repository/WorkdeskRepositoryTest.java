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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class WorkdeskRepositoryTest {

    @Autowired
    private WorkdeskProjectionRepository workdeskRepository;

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

        // Act
        // CA-14: Isolation test for tenantA (should return 2, ignore task3)
        // CA-17: Sorting test (task1 should be first because impactLevel 10 > 5)
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantA", null, null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("task1", result.getContent().get(0).getId()); // Highest impact
        assertEquals("task2", result.getContent().get(1).getId());
    }

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

        // Act
        // CA-10 / CA-19: ILike partial search
        Page<WorkdeskProjectionEntity> result = workdeskRepository.findWorkdeskTasks("tenantA", "ApPrOvAl", null, PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("task_search", result.getContent().get(0).getId());
    }
}
