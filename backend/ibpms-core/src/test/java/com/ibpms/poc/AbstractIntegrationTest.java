package com.ibpms.poc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para Pruebas de Integración End-to-End usando Testcontainers.
 * Instancia dinámicamente una Base de Datos MySQL efímera (Docker)
 * que sirve de backend para que Spring Boot y Camunda se levanten de forma
 * aislada.
 * 
 * Uso: Hacer que tus clases de test hereden de {@link AbstractIntegrationTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // Configuración estricta del Testcontainer para MySQL
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ibpms_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            // Aceleración de reinicio y deshabilitar reintentos lentos
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @BeforeAll
    static void startContainer() {
        if (!MYSQL_CONTAINER.isRunning()) {
            MYSQL_CONTAINER.start();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);

        // Evitamos que Camunda/Hibernate choquen con el contexto actual si hay ddl auto
        // activo
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @AfterAll
    static void stopContainer() {
        // En algunos casos Spring maneja la terminación de contenedores dinámicos,
        // pero podemos forzar el pare si es necesario para liberar memoria en CI/CD.
        MYSQL_CONTAINER.stop();
    }
}
