package com.ibpms.poc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.application.service.AutoClaimService;
import com.ibpms.poc.application.service.CamundaCompletionAdapter;
import com.ibpms.poc.application.service.FormCompletionService;
import com.ibpms.poc.application.service.SagaCompensationException;
import com.ibpms.poc.domain.model.EventType;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.FormEventRepository;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


public class FormCompletionSagaTest extends AbstractIntegrationTest {

    @Autowired
    private FormCompletionService formCompletionService;

    @Autowired
    private FormEventRepository formEventRepository;

    @Autowired
    private TaskDraftRepository taskDraftRepository;

    @MockBean
    private CamundaCompletionAdapter camundaCompletionAdapter;

    @MockBean
    private TaskService taskService;

    @MockBean
    private AutoClaimService autoClaimService;

    @BeforeEach
    void setupMock() {
        TaskQuery taskQuery = mock(TaskQuery.class);
        Task mockTask = mock(Task.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(any())).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);
        when(mockTask.getProcessInstanceId()).thenReturn("process-123");
    }

    @Test
    void testHappyPathSaga() {
        String taskId = "task-happy-" + UUID.randomUUID().toString();
        String userId = "user-1";
        
        FormSubmitRequest request = FormSubmitRequest.builder()
                .schemaVersion("v1")
                .payload(new HashMap<>())
                .idempotencyKey(UUID.randomUUID())
                .build();

        doNothing().when(camundaCompletionAdapter).completeTaskInEngine(eq(taskId), any());
        
        var response = formCompletionService.completeTask(taskId, request, userId);
        
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getEventReference().startsWith("EVT-"));

        List<FormEvent> events = formEventRepository.findAll().stream()
            .filter(e -> taskId.equals(e.getTaskId()))
            .toList();
        assertEquals(1, events.size());
        assertEquals(EventType.FORM_SUBMITTED, events.get(0).getEventType());
    }

    @Test
    void testRollbackSagaWhenCamundaFails() {
        String taskId = "task-fail-" + UUID.randomUUID().toString();
        String userId = "user-1";
        
        FormSubmitRequest request = FormSubmitRequest.builder()
                .schemaVersion("v1")
                .payload(new HashMap<>())
                .idempotencyKey(UUID.randomUUID())
                .build();

        doThrow(new RuntimeException("Camunda Engine timeout mock"))
                .when(camundaCompletionAdapter)
                .completeTaskInEngine(eq(taskId), any());
        
        assertThrows(SagaCompensationException.class, () -> {
            formCompletionService.completeTask(taskId, request, userId);
        });

        List<FormEvent> events = formEventRepository.findAll().stream()
            .filter(e -> taskId.equals(e.getTaskId()))
            .toList();
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getEventType() == EventType.FORM_SUBMITTED));
        assertTrue(events.stream().anyMatch(e -> e.getEventType() == EventType.FORM_SUBMIT_ROLLED_BACK));
        
        FormEvent rolledBackEvent = events.stream().filter(e -> e.getEventType() == EventType.FORM_SUBMIT_ROLLED_BACK).findFirst().get();
        FormEvent submittedEvent = events.stream().filter(e -> e.getEventType() == EventType.FORM_SUBMITTED).findFirst().get();
        assertEquals(submittedEvent.getEventId(), rolledBackEvent.getOriginalEventId());
    }
}
