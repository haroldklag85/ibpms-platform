// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.messaging;

import com.ibpms.poc.AbstractIntegrationTest;


import com.ibpms.poc.application.service.cache.AiDmnCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test de Integración para el RabbitListener FormSchemaChangedRabbitListener.
 * 
 * <p><strong>Ley Global 3 - Traceability Inversa:</strong></p>
 * <ul>
 *   <li><strong>Epic:</strong> Epic A (Motor Core & Task Management)</li>
 *   <li><strong>User Story:</strong> US-007 (Evaluación de Reglas de Negocio)</li>
 *   <li><strong>Criterio de Aceptación:</strong> CA-16 (Sincronización de Caché DMN) / T-15</li>
 *   <li><strong>Descripción:</strong> Valida que un evento de RabbitMQ para el cambio de un Form Schema dispare la purga asíncrona en caché de los DMN asociados utilizando un mock aislado del servicio AiDmnCacheService.</li>
 * </ul>
 */
// @Traceability: US-007, CA-16
@SpringBootTest
@ActiveProfiles("test")
public class FormSchemaChangedRabbitListenerTest extends AbstractIntegrationTest {

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
