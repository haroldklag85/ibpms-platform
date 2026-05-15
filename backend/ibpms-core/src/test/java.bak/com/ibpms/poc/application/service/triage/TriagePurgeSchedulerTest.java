package com.ibpms.poc.application.service.triage;

import com.ibpms.poc.application.service.TriagePurgeScheduler;
import com.ibpms.poc.domain.port.TriageTaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TriagePurgeSchedulerTest {

    @Test
    void purgeRejectedTriages_DeletesOlderThan30Days() {
        TriageTaskRepository repository = mock(TriageTaskRepository.class);
        TriagePurgeScheduler scheduler = new TriagePurgeScheduler(repository);

        ZonedDateTime expectedCutoff = ZonedDateTime.now().minusDays(30);

        scheduler.purgeRejectedTriages();

        ArgumentCaptor<ZonedDateTime> dateCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(repository, times(1)).deleteByStatusAndUpdatedAtBefore(eq("REJECTED"), dateCaptor.capture());

        ZonedDateTime capturedDate = dateCaptor.getValue();
        long diffSeconds = ChronoUnit.SECONDS.between(expectedCutoff, capturedDate);
        assertTrue(Math.abs(diffSeconds) < 5, "Cutoff date should be roughly 30 days ago");
    }
}
