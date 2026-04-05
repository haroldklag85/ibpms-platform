package com.ibpms.poc.infrastructure.config;

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
 * <p>Levanta contenedores Docker efímeros de PostgreSQL 16 y RabbitMQ 3
 * que replican fielmente el entorno de producción. Usa {@code @ServiceConnection}
 * de Spring Boot 3.1+ para inyectar automáticamente las propiedades de conexión
 * (JDBC URL, host, port, credentials) en el ApplicationContext.</p>
 *
 * <h3>Uso:</h3>
 * <pre>{@code
 * class MiTestDeIntegracion extends TestcontainersBaseIT {
 *     @Test
 *     void debeCrearTopologiaDLQ() {
 *         // El contexto Spring ya está conectado a PostgreSQL y RabbitMQ reales
 *     }
 * }
 * }</pre>
 *
 * <h3>Requisitos:</h3>
 * <ul>
 *   <li>Docker debe estar corriendo en la máquina/CI</li>
 *   <li>Liquibase ejecutará migrations contra PostgreSQL real</li>
 * </ul>
 */
@SpringBootTest
@Testcontainers
public abstract class TestcontainersBaseIT {

    /**
     * Contenedor PostgreSQL 16 Alpine.
     * Reemplaza H2 en tests de integración para evitar incompatibilidades
     * de dialecto (pg_trgm, gen_random_uuid(), locking behavior).
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("ibpms_test")
                    .withUsername("ibpms_test")
                    .withPassword("ibpms_test");

    /**
     * Contenedor RabbitMQ 3 con Management Plugin.
     * Permite validar topología real (exchanges, queues, bindings, DLX),
     * retry con backoff, y TTL de mensajes.
     */
    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit =
            new RabbitMQContainer("rabbitmq:3-management");
}
