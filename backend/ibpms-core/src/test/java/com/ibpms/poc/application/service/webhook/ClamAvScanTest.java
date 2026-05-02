package com.ibpms.poc.application.service.webhook;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookPayload;
import com.ibpms.poc.application.service.WebhookIntakeService.WebhookResponse;
import com.ibpms.poc.domain.port.*;
import com.ibpms.poc.infrastructure.config.WebhookProperties;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T6: ClamAvScanTest (US-004 CA-11)
 * Tests 3 scenarios via @MockBean: CLEAN, INFECTED, UNAVAILABLE.
 */
@ExtendWith(MockitoExtension.class)
class ClamAvScanTest {

    @Mock private WebhookTransactionRepository transactionRepo;
    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private AllowedDomainRepository domainRepo;
    @Mock private ClamAvScanner clamAvScanner;
    @Mock private RuntimeService runtimeService;

    private WebhookIntakeService service;

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        service = new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class));
    }

    private WebhookPayload createPayloadWithAttachment() {
        byte[] fakeFile = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR".getBytes();
        return new WebhookPayload("msg-scan", "user@ibm.com",
                "With attachment", "{}", fakeFile, "document.pdf", "default");
    }

    @Test
    @DisplayName("CA-11: CLEAN scan → file accepted, process created")
    void cleanFilePasses() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);
        when(clamAvScanner.scan(any(byte[].class), anyString())).thenReturn(ClamAvScanner.ScanResult.CLEAN);

        var mockInstance = mock(org.camunda.bpm.engine.runtime.ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-CLEAN");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), any(Map.class))).thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookResponse response = service.processIncomingWebhook(createPayloadWithAttachment());

        assertEquals(200, response.httpStatus());
        assertEquals("ACCEPTED", response.status());
    }

    @Test
    @DisplayName("CA-11: INFECTED scan → 422 + MALWARE_QUARANTINE in orphan_payloads")
    void infectedFileRejected() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);
        when(clamAvScanner.scan(any(byte[].class), anyString())).thenReturn(ClamAvScanner.ScanResult.INFECTED);
        when(orphanRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookResponse response = service.processIncomingWebhook(createPayloadWithAttachment());

        assertEquals(422, response.httpStatus());
        assertEquals("MALWARE_DETECTED", response.status());
        verify(orphanRepo, times(1)).save(argThat(o -> "MALWARE_QUARANTINE".equals(o.getErrorType())));
    }

    @Test
    @DisplayName("CA-11: Scanner UNAVAILABLE → 503 (fail-secure)")
    void scannerUnavailableFailsSecure() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);
        when(clamAvScanner.scan(any(byte[].class), anyString())).thenReturn(ClamAvScanner.ScanResult.UNAVAILABLE);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookResponse response = service.processIncomingWebhook(createPayloadWithAttachment());

        assertEquals(503, response.httpStatus());
        assertEquals("SCANNER_UNAVAILABLE", response.status());
        verify(runtimeService, never()).startProcessInstanceByKey(anyString(), anyString(), any(Map.class));
    }
}

