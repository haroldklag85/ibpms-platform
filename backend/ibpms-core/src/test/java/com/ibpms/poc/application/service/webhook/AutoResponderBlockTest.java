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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T2: AutoResponderBlockTest (US-004 CA-2)
 * Validates that system accounts (no-reply, mailer-daemon) are rejected at the perimeter.
 */
@ExtendWith(MockitoExtension.class)
class AutoResponderBlockTest {

    @Mock private WebhookTransactionRepository transactionRepo;
    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private AllowedDomainRepository domainRepo;
    @Mock private ClamAvScanner clamAvScanner;
    @Mock private RuntimeService runtimeService;

    private WebhookIntakeService service;

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        service = new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props, mock(com.ibpms.poc.application.service.IntegrationEventPublisher.class), mock(com.ibpms.poc.infrastructure.jpa.repository.TenantConfigRepository.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "no-reply@test.com",
            "noreply@company.org",
            "mailer-daemon@mx.google.com",
            "postmaster@outlook.com",
            "bounce@sendgrid.net"
    })
    @DisplayName("CA-2: Auto-responder emails are rejected with HTTP 400")
    void autoRespondersAreBlocked(String email) {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);

        WebhookPayload payload = new WebhookPayload("msg-autoresponder", email,
                "Auto-reply", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);

        assertEquals(400, response.httpStatus());
        assertEquals("AUTO_RESPONDER_BLOCKED", response.status());
        verify(runtimeService, never()).startProcessInstanceByKey(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("CA-2: Legitimate email passes auto-responder check")
    void legitimateEmailPassesCheck() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(true);

        var mockInstance = mock(org.camunda.bpm.engine.runtime.ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-OK");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), any(Map.class))).thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-legit", "pedro@ibm.com",
                "Legit email", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertNotEquals(400, response.httpStatus());
    }
}

