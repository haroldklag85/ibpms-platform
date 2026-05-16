package com.ibpms.poc.application.service.webhook;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookPayload;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookResponse;
import com.ibpms.poc.domain.port.*;
import com.ibpms.poc.infrastructure.config.WebhookProperties;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T8: PreTriageTaskCreationTest (US-004 CA-8, CA-9)
 * Validates that approved webhooks create a Pre-Triage task in Camunda
 * and the DTO minificado is passed correctly.
 */
@ExtendWith(MockitoExtension.class)
class PreTriageTaskCreationTest {

    @Mock private WebhookTransactionRepository transactionRepo;
    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private AllowedDomainRepository domainRepo;
    @Mock private ClamAvScanner clamAvScanner;
    @Mock private RuntimeService runtimeService;

    private WebhookIntakeService service;
    private static final String PROCESS_KEY = "Process_PreTriaje_Intake";

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        props.getPreTriage().setProcessDefinitionKey(PROCESS_KEY);
        service = new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class), mock(com.ibpms.poc.infrastructure.jpa.repository.TenantConfigRepository.class));
    }

    @Test
    @DisplayName("CA-8/CA-9: Valid webhook → Pre-Triage process instantiated (NOT definitive process)")
    void validWebhookCreatesPreTriageTask() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-TRIAGE-001");
        when(runtimeService.startProcessInstanceByKey(eq(PROCESS_KEY), anyString(), any(Map.class))).thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-triage", "client@ibm.com",
                "Intake Request", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);

        assertEquals(200, response.httpStatus());
        assertEquals("ACCEPTED", response.status());
        assertEquals("PI-TRIAGE-001", response.processInstanceId());
    }

    @Test
    @DisplayName("CA-8: Camunda receives DTO minificado (sender, subject, messageId, attachmentCount)")
    @SuppressWarnings("unchecked")
    void camundaReceivesMinifiedDto() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-DTO");

        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        when(runtimeService.startProcessInstanceByKey(eq(PROCESS_KEY), anyString(), varsCaptor.capture()))
                .thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clamAvScanner.scan(any(), anyString())).thenReturn(ClamAvScanner.ScanResult.CLEAN);

        byte[] attachment = "test-file-content".getBytes();
        WebhookPayload payload = new WebhookPayload("msg-dto", "analyst@ibm.com",
                "Intake Subject", "{}", attachment, "doc.pdf", "default");

        service.processIncomingWebhook(payload);

        Map<String, Object> capturedVars = varsCaptor.getValue();
        assertEquals("analyst@ibm.com", capturedVars.get("sender"));
        assertEquals("Intake Subject", capturedVars.get("subject"));
        assertEquals("msg-dto", capturedVars.get("messageId"));
        assertEquals(1, capturedVars.get("attachmentCount"));
    }

    @Test
    @DisplayName("CA-9: Engine failure → 500 + ENGINE_FAILURE status")
    void engineFailureReturns500() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), any(Map.class)))
                .thenThrow(new RuntimeException("Camunda down"));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-fail", "user@ibm.com",
                "Test", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(500, response.httpStatus());
        assertEquals("ENGINE_FAILURE", response.status());
    }
}

