package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.ProcessLockPort;
import com.ibpms.poc.infrastructure.jpa.entity.ProcessLockEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessLockJpaAdapterTest {

    @Mock
    private ProcessLockRepository repository;

    @InjectMocks
    private ProcessLockJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findLock_ReturnsInfoWhenExists() {
        ProcessLockEntity entity = new ProcessLockEntity();
        entity.setProcessDefinitionKey("key1");
        entity.setLockedBy("user1");
        entity.setLockedAt(LocalDateTime.now());
        entity.setBrowserSessionId("sess1");
        
        when(repository.findById("key1")).thenReturn(Optional.of(entity));

        Optional<ProcessLockPort.ProcessLockInfo> result = adapter.findLock("key1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().lockedBy());
    }

    @Test
    void saveLock_SavesEntity() {
        adapter.saveLock("key1", "user1", "sess1");
        verify(repository, times(1)).save(any(ProcessLockEntity.class));
    }

    @Test
    void deleteLock_DeletesEntity() {
        adapter.deleteLock("key1");
        verify(repository, times(1)).deleteById("key1");
    }
}
