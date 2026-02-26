package com.ibpms.poc.infrastructure.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración del Shared Transaction Manager.
 *
 * Garantía de consistencia ACID (ADR-003):
 * Un solo PlatformTransactionManager es compartido entre Spring Data JPA
 * (tablas ibpms_*)
 * y el motor Camunda 7 (tablas ACT_*). Si falla cualquier operación de negocio
 * dentro de
 * un @Transactional, AMBAS bases se hacen rollback de forma atómica.
 *
 * Mecanismo: Camunda 7 Spring Boot Starter auto-configura
 * SpringProcessEngineConfiguration.
 * Al inyectar el mismo DataSource y TransactionManager de Spring, Camunda usa
 * el mismo
 * contexto transaccional que JPA/Hibernate — sin JTA ni 2PC.
 */
@Configuration
public class CamundaEngineConfig extends AbstractCamundaConfiguration {

    private final PlatformTransactionManager transactionManager;

    public CamundaEngineConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void preInit(SpringProcessEngineConfiguration configuration) {
        // Compartir el mismo PlatformTransactionManager de Spring
        configuration.setTransactionManager(transactionManager);

        // Historia completa para auditoría (NFR-AUD-01)
        configuration.setHistory("full");
    }
}
