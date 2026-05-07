package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.DeployRequestPort;
import com.ibpms.poc.infrastructure.jpa.entity.DeployRequestEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DeployRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeployRequestJpaAdapterTest {

    @Mock
    private DeployRequestRepository repository;

    @InjectMocks
    private DeployRequestJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_ReturnsInfoWhenExists() {
        UUID id = UUID.randomUUID();
        DeployRequestEntity entity = new DeployRequestEntity();
        entity.setId(id);
        entity.setProcessDefinitionKey("proc1");
        entity.setStatus(DeployRequestEntity.Status.PENDING);
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Optional<DeployRequestPort.DeployRequestInfo> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals("proc1", result.get().processDefinitionKey());
    }

    @Test
    void save_SavesAndReturnsInfo() {
        UUID id = UUID.randomUUID();
        DeployRequestPort.DeployRequestInfo info = new DeployRequestPort.DeployRequestInfo(
            id, "proc1", "user1", null, "PENDING", null, null, null
        );
        
        DeployRequestEntity entity = new DeployRequestEntity();
        entity.setId(id);
        entity.setProcessDefinitionKey("proc1");
        
        when(repository.save(any(DeployRequestEntity.class))).thenReturn(entity);

        DeployRequestPort.DeployRequestInfo result = adapter.save(info);

        assertNotNull(result);
        assertEquals(id, result.id());
    }
}
