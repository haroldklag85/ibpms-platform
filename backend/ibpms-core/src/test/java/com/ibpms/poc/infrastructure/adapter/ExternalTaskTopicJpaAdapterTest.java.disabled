package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.domain.model.ExternalTaskTopic;
import com.ibpms.poc.infrastructure.jpa.entity.ExternalTaskTopicEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ExternalTaskTopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalTaskTopicJpaAdapterTest {

    @Mock
    private ExternalTaskTopicRepository repository;

    @InjectMocks
    private ExternalTaskTopicJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIsActiveTrue_ReturnsDomains() {
        ExternalTaskTopicEntity entity = new ExternalTaskTopicEntity();
        entity.setTopicName("topic1");
        entity.setIsActive(true);
        when(repository.findByIsActiveTrue()).thenReturn(List.of(entity));

        List<ExternalTaskTopic> result = adapter.findByIsActiveTrue();

        assertEquals(1, result.size());
        assertEquals("topic1", result.get(0).getTopicName());
    }
}
