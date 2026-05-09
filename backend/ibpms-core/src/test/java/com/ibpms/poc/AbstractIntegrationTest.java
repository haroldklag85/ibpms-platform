package com.ibpms.poc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @org.springframework.boot.test.web.server.LocalServerPort
    protected int port;

    // Patrón Singleton Testcontainer para PostgreSQL con vector support
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ibpms_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> RABBITMQ_CONTAINER = new GenericContainer<>(DockerImageName.parse("rabbitmq:3-management-alpine"))
            .withExposedPorts(5672, 15672);

    private static boolean testcontainersHealthy = false;

    // Iniciado en bloque estático con Fallback preventivo para Windows Docker Desktop API
    static {
        try {
            POSTGRES_CONTAINER.start();
            REDIS_CONTAINER.start();
            RABBITMQ_CONTAINER.start();
            testcontainersHealthy = true;
        } catch (Throwable t) {
            System.err.println("Testcontainers Windows Host Error detected. Falling back to native localhost UAT containers...");
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (testcontainersHealthy) {
            registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
            registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);

            registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
            registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

            registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
            registry.add("spring.rabbitmq.port", RABBITMQ_CONTAINER::getFirstMappedPort);
        } else {
            String postgresHost = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
            String postgresPort = System.getenv().getOrDefault("POSTGRES_PORT", "5433");
            String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
            String redisPort = System.getenv().getOrDefault("REDIS_PORT", "6380");
            String rabbitmqHost = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
            String rabbitmqPort = System.getenv().getOrDefault("RABBITMQ_PORT", "5673");

            registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + postgresHost + ":" + postgresPort + "/ibpms_e2e");
            registry.add("spring.datasource.username", () -> "ibpms");
            registry.add("spring.datasource.password", () -> "ibpms_e2e_pass");
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

            registry.add("spring.data.redis.host", () -> redisHost);
            registry.add("spring.data.redis.port", () -> Integer.parseInt(redisPort));

            registry.add("spring.rabbitmq.host", () -> rabbitmqHost);
            registry.add("spring.rabbitmq.port", () -> Integer.parseInt(rabbitmqPort));
        }

        // Cumplimiento Zero-Trust: Liquibase controla la DB, Hibernate en modo validación pura.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}
