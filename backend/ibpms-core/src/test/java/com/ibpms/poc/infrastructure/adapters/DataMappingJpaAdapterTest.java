package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.domain.model.DataMapping;
import com.ibpms.poc.infrastructure.jpa.entity.DataMappingEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DataMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataMappingJpaAdapterTest {

    @Mock
    private DataMappingRepository repository;

    @InjectMocks
    private DataMappingJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByProcessDefinitionKey_ReturnsDomains() {
        DataMappingEntity entity = new DataMappingEntity();
        entity.setId(UUID.randomUUID());
        entity.setProcessDefinitionKey("proc1");
        entity.setTaskId("task1");
        when(repository.findByProcessDefinitionKey("proc1")).thenReturn(List.of(entity));

        List<DataMapping> result = adapter.findByProcessDefinitionKey("proc1");

        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getTaskId());
    }

    @Test
    void save_ConvertsAndSaves() {
        DataMapping domain = new DataMapping();
        domain.setProcessDefinitionKey("proc1");
        
        DataMappingEntity entity = new DataMappingEntity();
        entity.setId(UUID.randomUUID());
        entity.setProcessDefinitionKey("proc1");
        when(repository.save(any(DataMappingEntity.class))).thenReturn(entity);

        DataMapping result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("proc1", result.getProcessDefinitionKey());
    }
}
