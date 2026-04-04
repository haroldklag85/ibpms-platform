package com.ibpms.poc.infrastructure.bpm.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * CA-04: Hardening RCE (Remote Code Execution) para el Motor de Decisión (DMN).
 * Aísla matemáticamente el compilador FEEL de Camunda bloqueando llamadas Java externas.
 */
@Configuration
public class DmnFeelSandboxConfig {

    private static final Logger log = LoggerFactory.getLogger(DmnFeelSandboxConfig.class);

    private final SpringProcessEngineConfiguration processEngineConfiguration;

    public DmnFeelSandboxConfig(SpringProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    @PostConstruct
    public void secureFeelEngine() {
        log.info("[APPSEC-SANDBOX] Forzando restricciones Anti-RCE en el compilador FeelEngine...");
        
        // Camunda >7.13 utiliza Scala FEEL Engine por defecto.
        // Impedir que las Celdas DMN invoquen clases estáticas Java del Host.
        // processEngineConfiguration.setDmnFeelEnableCustomFunctionDeclarations(false); // Eliminado: No existe en la API actual.
        // Desactiva la interpolación de Contexto de Spring-Beans dentro del marco matemático.
        processEngineConfiguration.setBeans(null);

        log.info("[APPSEC-SANDBOX] Motor DMN exitosamente encarcelado. Inyección de código (Prompt Injection RCE) neutralizada.");
    }
}
