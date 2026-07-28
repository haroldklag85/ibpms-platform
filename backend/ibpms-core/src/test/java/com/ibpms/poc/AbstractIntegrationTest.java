// @Traceability: US-007 - ADR-001
package com.ibpms.poc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Clase base para Pruebas de Integración End-to-End usando Testcontainers.
 * Provee la infraestructura efímera (PostgreSQL, Redis, RabbitMQ) compartida
 * por todas las subclases.
 * 
 * NOTA ARQUITECTÓNICA (2026-04-30): Esta clase NO declara @LocalServerPort.
 * Los tests que necesiten el puerto (RestAssured) deben declararlo localmente.
 * Los tests MockMvc que heredan de esta clase pueden override @SpringBootTest
 * a WebEnvironment.MOCK sin conflicto de inyección.
 * 
 * Uso: Hacer que tus clases de test hereden de {@link AbstractIntegrationTest}.
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {


    @org.springframework.boot.test.web.server.LocalServerPort
    protected int port;

    // @Traceability: US-005, CA-65
    // Remediación ADR-010: Eliminado Testcontainers. Conexión obligatoria a contenedores Docker estáticos.

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String postgresHost = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        String rabbitmqHost = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");

        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + postgresHost + ":5434/ibpms");
        registry.add("spring.datasource.username", () -> "ibpms");
        registry.add("spring.datasource.password", () -> "ibpms_e2e_pass");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> 6380); // Puerto estático Redis e2e

        registry.add("spring.rabbitmq.host", () -> rabbitmqHost);
        registry.add("spring.rabbitmq.port", () -> 5673); // Puerto estático RabbitMQ e2e

        // Cumplimiento Zero-Trust: Liquibase controla la DB, Hibernate en modo validación pura.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("camunda.bpm.database.schema-update", () -> "true");
    }
}
