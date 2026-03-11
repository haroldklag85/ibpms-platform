package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.WorkdeskGlobalItemDTO;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkdeskQueryPerformanceTest {

    @Mock
    private WorkdeskProjectionRepository repository;

    @InjectMocks
    private WorkdeskQueryController controller;

    private List<WorkdeskProjectionEntity> mockLargeDataset;

    @BeforeEach
    void setUp() {
        // Generar 10,000 registros para simular un volumen respetable
        mockLargeDataset = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            WorkdeskProjectionEntity entity = new WorkdeskProjectionEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setSourceSystem(i % 2 == 0 ? "BPMN" : "KANBAN");
            entity.setOriginalTaskId("TASK-" + i);
            entity.setTitle("Task Title " + i);
            entity.setSlaExpirationDate(LocalDateTime.now().plusHours(i));
            entity.setStatus("ACTIVE");
            entity.setAssignee("user-" + (i % 50));
            mockLargeDataset.add(entity);
        }
    }

    @Test
    void testPerformanceNfrPer01_WorkdeskQuery_Sub800ms() {
        // Arrange: Mock the JPA page response
        PageRequest pageRequest = PageRequest.of(0, 50);
        Page<WorkdeskProjectionEntity> pagedResponse = new PageImpl<>(mockLargeDataset.subList(0, 50), pageRequest, mockLargeDataset.size());
        when(repository.findAll(pageRequest)).thenReturn(pagedResponse);

        // Act & Measure Latency
        long startTime = System.currentTimeMillis();
        
        ResponseEntity<Page<WorkdeskGlobalItemDTO>> response = controller.getGlobalInbox(pageRequest);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert: NFR-PER-01 (<= 800ms)
        System.out.println("Execution Time (ms): " + executionTime);
        assertTrue(executionTime <= 800, "Violación NFR-PER-01: El tiempo de respuesta (" + executionTime + "ms) excedió los 800ms permitidos.");
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().getContent().size() == 50);
    }
}
