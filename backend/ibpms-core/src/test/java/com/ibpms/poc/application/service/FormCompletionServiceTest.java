package com.ibpms.poc.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.FormSubmitRequest;
import com.ibpms.poc.application.dto.FormSubmitResponse;
import com.ibpms.poc.domain.model.FormEvent;
import com.ibpms.poc.domain.port.DocumentSecurityPort;
import com.ibpms.poc.domain.port.FormEventRepository;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import com.ibpms.poc.infrastructure.security.PiiEncryptionService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FormCompletionServiceTest {

    private AutoClaimService autoClaimService;
    private CamundaCompletionAdapter camundaCompletionAdapter;
    private FormEventRepository formEventRepository;
    private TaskDraftRepository taskDraftRepository;
    private TaskService taskService;
    private ObjectMapper objectMapper;
    private PiiEncryptionService piiEncryptionService;
    private EventReferenceGenerator eventReferenceGenerator;
    private DocumentSecurityPort documentSecurityPort;
    private FormCompletionService formCompletionService;

    @BeforeEach
    void setUp() {
        autoClaimService = mock(AutoClaimService.class);
        camundaCompletionAdapter = mock(CamundaCompletionAdapter.class);
        formEventRepository = mock(FormEventRepository.class);
        taskDraftRepository = mock(TaskDraftRepository.class);
        taskService = mock(TaskService.class);
        objectMapper = new ObjectMapper();
        piiEncryptionService = mock(PiiEncryptionService.class);
        eventReferenceGenerator = mock(EventReferenceGenerator.class);
        documentSecurityPort = mock(DocumentSecurityPort.class);

        formCompletionService = new FormCompletionService(
                autoClaimService,
                camundaCompletionAdapter,
                formEventRepository,
                taskDraftRepository,
                taskService,
                objectMapper,
                piiEncryptionService,
                eventReferenceGenerator,
                documentSecurityPort
        );
    }

    @Test
    void testCompleteTaskSuccess() throws Exception {
        String taskId = "task-1";
        String userId = "user-1";

        FormSubmitRequest request = new FormSubmitRequest();
        Map<String, Object> payload = new HashMap<>();
        payload.put("field1", "value1");
        request.setPayload(payload);
        request.setSchemaVersion("1.0");

        Task taskMock = mock(Task.class);
        when(taskMock.getProcessInstanceId()).thenReturn("proc-1");

        TaskQuery taskQueryMock = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQueryMock);
        when(taskQueryMock.taskId(taskId)).thenReturn(taskQueryMock);
        when(taskQueryMock.singleResult()).thenReturn(taskMock);

        when(piiEncryptionService.encrypt(anyString())).thenReturn("ENCRYPTED_DATA");
        when(eventReferenceGenerator.generateFromId(any(UUID.class))).thenReturn("EVT-123456");

        FormSubmitResponse response = formCompletionService.completeTask(taskId, request, userId);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("EVT-123456", response.getEventReference());

        verify(autoClaimService).tryAutoClaim(taskId, userId);
        verify(camundaCompletionAdapter).completeTaskInEngine(eq(taskId), any());
        
        ArgumentCaptor<FormEvent> eventCaptor = ArgumentCaptor.forClass(FormEvent.class);
        verify(formEventRepository, times(1)).save(eventCaptor.capture());
        
        FormEvent savedEvent = eventCaptor.getValue();
        assertEquals("proc-1", savedEvent.getProcessInstanceId());
        assertTrue(savedEvent.getPayloadJson().contains("ENCRYPTED_DATA"));
    }

    @Test
    void testCompleteTaskThrowsExceptionWhenTaskNotFound() {
        String taskId = "task-notFound";
        String userId = "user-1";
        
        FormSubmitRequest request = new FormSubmitRequest();

        TaskQuery taskQueryMock = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQueryMock);
        when(taskQueryMock.taskId(taskId)).thenReturn(taskQueryMock);
        when(taskQueryMock.singleResult()).thenReturn(null);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            formCompletionService.completeTask(taskId, request, userId);
        });

        assertEquals("Task not found: " + taskId, thrown.getMessage());
    }

    @Test
    void testAntiIdorVerificationIsTriggeredForUUID() throws Exception {
        String taskId = "task-1";
        String userId = "user-1";
        UUID docId = UUID.randomUUID();

        FormSubmitRequest request = new FormSubmitRequest();
        Map<String, Object> payload = new HashMap<>();
        // Inyectando un UUID en el payload
        payload.put("fileId", docId.toString());
        request.setPayload(payload);

        Task taskMock = mock(Task.class);
        TaskQuery taskQueryMock = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQueryMock);
        when(taskQueryMock.taskId(taskId)).thenReturn(taskQueryMock);
        when(taskQueryMock.singleResult()).thenReturn(taskMock);
        
        when(piiEncryptionService.encrypt(anyString())).thenReturn("ENCRYPTED_DATA");

        formCompletionService.completeTask(taskId, request, userId);

        // Verificamos que se llamó al puerto de seguridad para confirmar el Ownership
        verify(documentSecurityPort).confirmOwnershipAndMarkConfirmed(docId, taskId, userId);
    }
    
    @Test
    void testSagaRollbackOnCamundaFailure() throws Exception {
        String taskId = "task-1";
        String userId = "user-1";

        FormSubmitRequest request = new FormSubmitRequest();
        Map<String, Object> payload = new HashMap<>();
        payload.put("field1", "value1");
        request.setPayload(payload);
        request.setSchemaVersion("1.0");

        Task taskMock = mock(Task.class);
        when(taskMock.getProcessInstanceId()).thenReturn("proc-1");

        TaskQuery taskQueryMock = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQueryMock);
        when(taskQueryMock.taskId(taskId)).thenReturn(taskQueryMock);
        when(taskQueryMock.singleResult()).thenReturn(taskMock);

        when(piiEncryptionService.encrypt(anyString())).thenReturn("ENCRYPTED_DATA");
        
        doThrow(new RuntimeException("Camunda error"))
            .when(camundaCompletionAdapter).completeTaskInEngine(eq(taskId), any());

        assertThrows(SagaCompensationException.class, () -> {
            formCompletionService.completeTask(taskId, request, userId);
        });

        // 2 eventos debieron ser guardados: SUBMITTED y ROLLED_BACK
        verify(formEventRepository, times(2)).save(any(FormEvent.class));
    }
}
