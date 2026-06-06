// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.config;

import com.ibpms.poc.AbstractIntegrationTest;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base reutilizable para tests de integración con Testcontainers.
 *
 * <p>Cierre de hallazgo: Testing Stack Audit - Nivel 3 (Integración)</p>
 *
 * <p>ADR-010 EXCEPCIÓN: Enfoque E2E Estático Activo.
 * Debido a restricciones de memoria local, Testcontainers ha sido desactivado.
 * Los tests usarán la infraestructura estática del docker-compose.e2e.yml
 * configurada en application-test.yml.</p>
 */
@SpringBootTest
// @Testcontainers // Desactivado por ADR-010 (Enfoque E2E Estático)
public abstract class TestcontainersBaseIT extends AbstractIntegrationTest {

    /**
     * Contenedor PostgreSQL 16 Alpine.
     */
    // @Container
    // @ServiceConnection
    // @SuppressWarnings("resource")
    // static PostgreSQLContainer<?> postgres =
    //         new PostgreSQLContainer<>("postgres:16-alpine")
    //                 .withDatabaseName("ibpms_test")
    //                 .withUsername("ibpms_test")
    //                 .withPassword("ibpms_test");

    /**
     * Contenedor RabbitMQ 3 con Management Plugin.
     */
    // @Container
    // @ServiceConnection
    // static RabbitMQContainer rabbit =
    //         new RabbitMQContainer("rabbitmq:3-management-alpine");
}
