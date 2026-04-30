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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T4: WhitelistDomainTest (US-004 CA-4, CA-12)
 * Validates domain whitelist allows registered and blocks unregistered domains.
 */
@ExtendWith(MockitoExtension.class)
class WhitelistDomainTest {

    @Mock private WebhookTransactionRepository transactionRepo;
    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private AllowedDomainRepository domainRepo;
    @Mock private ClamAvScanner clamAvScanner;
    @Mock private RuntimeService runtimeService;

    private WebhookIntakeService service;

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        service = new WebhookIntakeService(transactionRepo, orphanRepo, domainRepo, mock(com.ibpms.poc.domain.port.TriageTaskRepository.class), clamAvScanner, runtimeService, props);
    }

    @Test
    @DisplayName("CA-4: Registered domain → pass (allowed)")
    void registeredDomainPasses() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue("@ibm.com", "default")).thenReturn(true);

        var mockInstance = mock(org.camunda.bpm.engine.runtime.ProcessInstance.class);
        when(mockInstance.getProcessInstanceId()).thenReturn("PI-WL");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), any(Map.class))).thenReturn(mockInstance);
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-wl-ok", "client@ibm.com",
                "Test", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(200, response.httpStatus());
    }

    @Test
    @DisplayName("CA-4: Unregistered domain → 403 Forbidden")
    void unregisteredDomainBlocked() {
        when(transactionRepo.existsByMessageId(anyString())).thenReturn(false);
        when(domainRepo.existsByDomainAndTenantIdAndIsActiveTrue("@random.xyz", "default")).thenReturn(false);
        when(orphanRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookPayload payload = new WebhookPayload("msg-wl-bad", "rogue@random.xyz",
                "Spam", "{}", null, null, "default");

        WebhookResponse response = service.processIncomingWebhook(payload);
        assertEquals(403, response.httpStatus());
        assertEquals("DOMAIN_NOT_AUTHORIZED", response.status());
    }
}

