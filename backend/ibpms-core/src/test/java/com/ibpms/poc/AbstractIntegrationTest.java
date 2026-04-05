package com.ibpms.poc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base para Pruebas de Integración End-to-End usando Testcontainers.
 * Instancia dinámicamente una Base de Datos MySQL efímera (Docker)
 * que sirve de backend para que Spring Boot y Camunda se levanten de forma
 * aislada.
 * 
 * Uso: Hacer que tus clases de test hereden de {@link AbstractIntegrationTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // Patrón Singleton Testcontainer para PostgreSQL con vector support
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ibpms_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    // Iniciado en bloque estático (Singleton: El TestJVM nunca lo detiene hasta finalizar suite)
    static {
        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);

        // Cumplimiento Zero-Trust: Liquibase controla la DB, Hibernate en modo validación pura.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
