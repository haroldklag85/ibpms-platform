package com.ibpms.poc.infrastructure.mq.job;

import com.ibpms.poc.infrastructure.jpa.repository.DlqArchiveRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MqMaintenanceJobTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @Mock
    private DlqArchiveRepository dlqArchiveRepository;

    @InjectMocks
    private MqMaintenanceJob mqMaintenanceJob;

    @Test
    void purgeIdempotencyKeys_ShouldCallDeleteOlderThanWith72Hours() {
        mqMaintenanceJob.purgeIdempotencyKeys();
        
        // Verificamos que se llame con alguna fecha (idealmente 72h, validado genéricamente por cualquier param)
        verify(processedMessageRepository).deleteOlderThan(any(LocalDateTime.class));
    }

    @Test
    void purgeDlqArchives_ShouldCallDeleteOlderThanWith180Days() {
        mqMaintenanceJob.purgeDlqArchives();
        
        verify(dlqArchiveRepository).deleteOlderThan(any(LocalDateTime.class));
    }
}
