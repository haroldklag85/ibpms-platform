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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T3: MalformedPayloadTest (US-004 CA-3)
 * Validates that malformed/unauthorized payloads are logged in orphan_payloads.
 */
@ExtendWith(MockitoExtension.class)
class MalformedPayloadTest {

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

    @Test
    @DisplayName("CA-3: Payload from unauthorized domain → 403 + orphan_payloads record")
    void unauthorizedDomainCreatesOrphanRecord() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue(anyString(), anyString())).thenReturn(false);
        when(orphanRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-bad-domain", "hacker@evil.com",
                "Malicious", "{\"data\": \"garbage\"}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);

        assertEquals(403, response.httpStatus());
        assertEquals("DOMAIN_NOT_AUTHORIZED", response.status());
        verify(orphanRepo, times(1)).save(argThat(orphan ->
                "UNAUTHORIZED".equals(orphan.getErrorType()) &&
                "hacker@evil.com".equals(orphan.getSenderEmail())
        ));
    }
}

