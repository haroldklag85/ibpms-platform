package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
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

class BpmnDesignJpaAdapterTest {

    @Mock
    private BpmnProcessDesignRepository repository;

    @InjectMocks
    private BpmnDesignJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_ReturnsDomainWhenExists() {
        UUID id = UUID.randomUUID();
        BpmnProcessDesignEntity entity = new BpmnProcessDesignEntity();
        entity.setId(id);
        entity.setTechnicalId("tech1");
        entity.setStatus("DRAFT");
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Optional<BpmnProcessDesign> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findByTechnicalId_ReturnsDomainWhenExists() {
        BpmnProcessDesignEntity entity = new BpmnProcessDesignEntity();
        entity.setTechnicalId("tech1");
        when(repository.findByTechnicalId("tech1")).thenReturn(Optional.of(entity));

        Optional<BpmnProcessDesign> result = adapter.findByTechnicalId("tech1");

        assertTrue(result.isPresent());
        assertEquals("tech1", result.get().getTechnicalId());
    }

    @Test
    void save_ConvertsAndSaves() {
        BpmnProcessDesign domain = new BpmnProcessDesign();
        domain.setTechnicalId("tech1");
        
        BpmnProcessDesignEntity entity = new BpmnProcessDesignEntity();
        entity.setTechnicalId("tech1");
        
        when(repository.save(any(BpmnProcessDesignEntity.class))).thenReturn(entity);

        BpmnProcessDesign result = adapter.save(domain);

        assertNotNull(result);
        assertEquals("tech1", result.getTechnicalId());
        verify(repository, times(1)).save(any(BpmnProcessDesignEntity.class));
    }
}
