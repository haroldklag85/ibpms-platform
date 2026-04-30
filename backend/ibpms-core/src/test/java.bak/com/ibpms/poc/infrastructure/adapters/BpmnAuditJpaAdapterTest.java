package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BpmnAuditJpaAdapterTest {

    @Mock
    private BpmnDesignAuditLogRepository repository;

    @InjectMocks
    private BpmnAuditJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logAction_SavesEntity() {
        UUID processDesignId = UUID.randomUUID();
        adapter.logAction(processDesignId, "TEST_ACTION", "user1", 1, "details");
        verify(repository, times(1)).save(any(BpmnDesignAuditLogEntity.class));
    }
}
