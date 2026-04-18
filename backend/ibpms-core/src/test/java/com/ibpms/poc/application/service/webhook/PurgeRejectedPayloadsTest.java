package com.ibpms.poc.application.service.webhook;

import com.ibpms.poc.application.service.WebhookIntakeService;
import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import com.ibpms.poc.infrastructure.config.WebhookProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ibpms.poc.domain.port.TriageTaskRepository;

/**
 * T9: PurgeRejectedPayloadsTest (US-004 CA-13)
 * Validates that orphan payloads older than 30 days are physically purged.
 */
@ExtendWith(MockitoExtension.class)
class PurgeRejectedPayloadsTest {

    @Mock private OrphanPayloadRepository orphanRepo;
    @Mock private TriageTaskRepository triageTaskRepository;

    private WebhookIntakeService service;

    @BeforeEach
    void setUp() {
        WebhookProperties props = new WebhookProperties();
        service = new WebhookIntakeService(null, orphanRepo, null, triageTaskRepository, null, null, props);
    }

    @Test
    @DisplayName("CA-13: Purge deletes payloads older than 30 days")
    void purgeDeletesOldPayloads() {
        ArgumentCaptor<ZonedDateTime> cutoffCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);

        service.purgeExpiredOrphanPayloads();

        verify(orphanRepo, times(1)).deleteByCreatedAtBefore(cutoffCaptor.capture());

        ZonedDateTime capturedCutoff = cutoffCaptor.getValue();
        ZonedDateTime thirtyDaysAgo = ZonedDateTime.now().minusDays(30);

        // The cutoff should be approximately 30 days ago (within 1 minute tolerance)
        assertTrue(capturedCutoff.isBefore(thirtyDaysAgo.plusMinutes(1)));
        assertTrue(capturedCutoff.isAfter(thirtyDaysAgo.minusMinutes(1)));
    }

    @Test
    @DisplayName("CA-13: Purge is called without errors on empty repository")
    void purgeOnEmptyRepositorySucceeds() {
        doNothing().when(orphanRepo).deleteByCreatedAtBefore(any());

        assertDoesNotThrow(() -> service.purgeExpiredOrphanPayloads());
        verify(orphanRepo, times(1)).deleteByCreatedAtBefore(any());
    }
}

