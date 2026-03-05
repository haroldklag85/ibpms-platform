package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.ProjectTemplateDTO;
import com.ibpms.poc.infrastructure.jpa.repository.ProjectTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateHierarchyIntegrityTest {

    @Mock
    private ProjectTemplateRepository repository;

    @InjectMocks
    private CrearProjectTemplateService service;

    private ProjectTemplateDTO buildCyclicPayload() {
        ProjectTemplateDTO dto = new ProjectTemplateDTO();
        dto.setName("Cyclic WBS Test");

        ProjectTemplateDTO.PhaseDTO phase = new ProjectTemplateDTO.PhaseDTO();
        ProjectTemplateDTO.MilestoneDTO milestone = new ProjectTemplateDTO.MilestoneDTO();

        ProjectTemplateDTO.TaskDTO taskA = new ProjectTemplateDTO.TaskDTO();
        taskA.setId("task-A");

        ProjectTemplateDTO.TaskDTO taskB = new ProjectTemplateDTO.TaskDTO();
        taskB.setId("task-B");

        // Create Cycle: A depends on B, and B depends on A
        ProjectTemplateDTO.DependencyDTO depAtoB = new ProjectTemplateDTO.DependencyDTO();
        depAtoB.setSourceTaskId("task-A");
        depAtoB.setTargetTaskId("task-B");

        ProjectTemplateDTO.DependencyDTO depBtoA = new ProjectTemplateDTO.DependencyDTO();
        depBtoA.setSourceTaskId("task-B");
        depBtoA.setTargetTaskId("task-A");

        taskA.setDependencies(List.of(depAtoB));
        taskB.setDependencies(List.of(depBtoA));

        milestone.setTasks(List.of(taskA, taskB));
        phase.setMilestones(List.of(milestone));
        dto.setPhases(List.of(phase));

        return dto;
    }

    @Test
    void whenPayloadHasCyclicDependencies_thenThrowsIllegalArgumentException() {
        ProjectTemplateDTO payload = buildCyclicPayload();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.crearPlantilla(payload, "Admin");
        });

        assertTrue(ex.getMessage().contains("Cyclic Dependencies Detected (HTTP 400)"));
    }
}
