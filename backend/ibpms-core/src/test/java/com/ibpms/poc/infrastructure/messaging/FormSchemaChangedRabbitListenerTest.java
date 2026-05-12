package com.ibpms.poc.infrastructure.messaging;

import com.ibpms.poc.application.service.cache.AiDmnCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @Traceability: US-007, CA-16
@SpringBootTest
@ActiveProfiles("test")
public class FormSchemaChangedRabbitListenerTest {

    @MockBean
    private AiDmnCacheService aiDmnCacheService;

    @Autowired
    private FormSchemaChangedRabbitListener formSchemaChangedRabbitListener;

    @Test
    public void testOnFormSchemaChanged_EvictsCache() {
        // Arrange
        String dummyPayload = "{\"schemaId\":\"123\", \"version\":\"v2\"}";

        // Act
        formSchemaChangedRabbitListener.onFormSchemaChanged(dummyPayload);

        // Assert
        verify(aiDmnCacheService, times(1)).evictAll();
    }
}
