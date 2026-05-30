// @Traceability: US-003 - ADR-001
package com.ibpms.poc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Clase base para Pruebas de Integración End-to-End conectadas a la infraestructura estática
 * (docker-compose.e2e.yml) sin usar Testcontainers (Cumplimiento de Hardware Limits y ADR-010 V2).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractLocalE2ETest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String postgresHost = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        String rabbitmqHost = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");

        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + postgresHost + ":5433/ibpms_e2e");
        registry.add("spring.datasource.username", () -> "ibpms");
        registry.add("spring.datasource.password", () -> "ibpms_e2e_pass");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> 6380);

        registry.add("spring.rabbitmq.host", () -> rabbitmqHost);
        registry.add("spring.rabbitmq.port", () -> 5673);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("camunda.bpm.database.schema-update", () -> "true");
    }
}
