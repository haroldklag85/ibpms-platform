package com.ibpms.poc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Clase base para Pruebas de Integración End-to-End conectadas a la infraestructura estática
 * (docker-compose.e2e.yml) sin usar Testcontainers (Cumplimiento de Hardware Limits y ADR-010 V2).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractLocalE2ETest {
}
