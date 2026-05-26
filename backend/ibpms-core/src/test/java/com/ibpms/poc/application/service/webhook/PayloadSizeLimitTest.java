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
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T7: PayloadSizeLimitTest (US-004 CA-7)
 * Validates payload size enforcement against parametric limit.
 */
@ExtendWith(MockitoExtension.class)
class PayloadSizeLimitTest {

    @Mock private WebhookTransactionRepository transactionRepo;
    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private AllowedDomainRepository domainRepo;
    @Mock private ClamAvScanner clamAvScanner;
    @Mock private RuntimeService runtimeService;

    private WebhookIntakeService service;

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        props.getPayload().setMaxSizeBytes(1024); // 1KB limit for testing
        service = new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class), mock(com.ibpms.poc.infrastructure.jpa.repository.TenantConfigRepository.class));
    }

    @Test
    @DisplayName("CA-7: Payload within limit → allowed")
    void payloadWithinLimitPasses() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        var mockInstance = mock(org.camunda.bpm.engine.runtime.ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-SIZE-OK");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), any(Map.class))).thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 100 bytes — well within 1KB limit
        WebhookPayload payload = new WebhookPayload("msg-size-ok", "user@ibm.com",
                "Small payload", "x".repeat(100), null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(200, response.httpStatus());
    }

    @Test
    @DisplayName("CA-7: Payload exceeding limit → 413 Payload Too Large")
    void payloadExceedingLimitRejected() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        // 2000 bytes of body — exceeds 1KB limit
        WebhookPayload payload = new WebhookPayload("msg-size-bad", "user@ibm.com",
                "Huge payload", "x".repeat(2000), null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(413, response.httpStatus());
        assertEquals("PAYLOAD_TOO_LARGE", response.status());
        verify(runtimeService, never()).startProcessInstanceByKey(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("CA-7: Attachment bytes pushing total over limit → 413")
    void attachmentPushesOverLimit() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        // Small body but 2KB attachment → over 1KB limit
        byte[] largeAttachment = new byte[2048];
        WebhookPayload payload = new WebhookPayload("msg-attach-big", "user@ibm.com",
                "With big file", "{}", largeAttachment, "big.pdf", "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(413, response.httpStatus());
    }
}

