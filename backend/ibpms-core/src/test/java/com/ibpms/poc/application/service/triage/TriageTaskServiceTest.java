package com.ibpms.poc.application.service.triage;

import com.ibpms.poc.application.service.TriageTaskService;
import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TriageTaskServiceTest {

    private TriageTaskRepository repository;
    private RuntimeService runtimeService;
    private TriageTaskService service;

    @BeforeEach
    void setUp() {
        repository = mock(TriageTaskRepository.class);
        runtimeService = mock(RuntimeService.class);
        service = new TriageTaskService(repository, runtimeService);
    }

    @Test
    void testApproveTask_Success_TriggersNewProcessAndTerminatesOld() {
        UUID taskId = UUID.randomUUID();
        TriageTask pendingTask = TriageTask.builder()
                .id(taskId)
                .camundaProcessInstanceId("OLD_PROCESS_123")
                .status("PENDING")
                .createdAt(ZonedDateTime.now())
                .build();

        when(repository.findByIdForUpdate(taskId)).thenReturn(Optional.of(pendingTask));
        when(repository.save(any(TriageTask.class))).thenAnswer(i -> i.getArgument(0));

        TriageTask result = service.approveTask(taskId, "onboarding_process");

        assertEquals("APPROVED", result.getStatus());
        verify(runtimeService, times(1)).startProcessInstanceByKey("onboarding_process");
        verify(runtimeService, times(1)).deleteProcessInstance("OLD_PROCESS_123", "Triaje Aprobado");
    }

    @Test
    void testRejectTask_Success_SavesReasonAndTerminatesProcess() {
        UUID taskId = UUID.randomUUID();
        TriageTask pendingTask = TriageTask.builder()
                .id(taskId)
                .camundaProcessInstanceId("OLD_PROCESS_456")
                .status("PENDING")
                .createdAt(ZonedDateTime.now())
                .build();

        when(repository.findByIdForUpdate(taskId)).thenReturn(Optional.of(pendingTask));
        when(repository.save(any(TriageTask.class))).thenAnswer(i -> i.getArgument(0));

        TriageTask result = service.rejectTask(taskId, "Spam evadido");

        assertEquals("REJECTED", result.getStatus());
        assertEquals("Spam evadido", result.getRejectionReason());
        verify(runtimeService, times(1)).deleteProcessInstance("OLD_PROCESS_456", "Triaje Rechazado: Spam evadido");
    }

    @Test
    void testApproveTask_ThrowsBadRequest_WhenProcessTypeMissing() {
        UUID taskId = UUID.randomUUID();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> 
            service.approveTask(taskId, "")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testRejectTask_ThrowsBadRequest_WhenReasonMissing() {
        UUID taskId = UUID.randomUUID();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> 
            service.rejectTask(taskId, "   ")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
