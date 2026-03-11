package com.ibpms.poc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

/**
 * Pruebas de Integración (End-to-End locales).
 * Utiliza Testcontainers con MySQL 8 real para asegurar
 * compatibilidad de JPA y Camunda. Se simula el Token JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CaseToTaskIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("test_ibpms")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        // Hibernate auto-create para pruebas, ignorando Liquibase momentáneamente
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String validJwtToken;

    @BeforeEach
    void setup() {
        // Generar JWT temporal asimulado para el test de MVC
        validJwtToken = jwtTokenProvider.generateToken("user_tester", List.of("USER"));
    }

    @Test
    void testEndToEnd_Seguridad_Camunda_Catalogos() throws Exception {

        // --- 1. Probar que sin Token de seguridad deniega el acceso ---
        mockMvc.perform(get("/api/v1/catalogs/countries"))
                .andExpect(status().isForbidden()); // 403 Forbidden por Spring Security

        // --- 2. Probar Catálogo con Token ---
        mockMvc.perform(get("/api/v1/catalogs/countries")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", java.util.Objects.requireNonNull(hasSize(3))))
                .andExpect(jsonPath("$[0].code").value("CO"));

        // --- 3. Crear Expediente fallando limpiamente vía GlobalExceptionHandler ---
        ExpedienteDTO request = new ExpedienteDTO();
        request.setDefinitionKey("dummy-process");
        request.setBusinessKey(UUID.randomUUID().toString());
        request.setType("PRUEBA");
        request.setVariables(Map.of("clienteId", "C-999"));

        String idempotencyKey = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/expedientes")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                // Debido a que "dummy-process" no está desplegado en Camunda para este test,
                // esperamos el 500 elegante (o si lo capturamos como NotFound, el que responda)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno del servidor"));
    }
}
