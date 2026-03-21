package com.ibpms.poc.application.usecase.ai;

import com.ibpms.poc.infrastructure.jpa.entity.ai.ForensicIsoOverrideEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ai.ForensicIsoOverrideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * CA-09: Servicio transaccional Write-Only para la toma forense de directivas ISO.
 */
@Service
public class ForensicIsoOverrideUseCase {

    private static final Logger log = LoggerFactory.getLogger(ForensicIsoOverrideUseCase.class);
    private final ForensicIsoOverrideRepository forensicRepository;

    public ForensicIsoOverrideUseCase(ForensicIsoOverrideRepository forensicRepository) {
        this.forensicRepository = forensicRepository;
    }

    /**
     * Ingiere de forma inmutable la responsabilidad del arquitecto.
     * Genera una estampilla tipo "Logger ERROR" para ser acoplada al stack de SOC/Datadog.
     */
    @Transactional
    public void recordInfraction(String userId, Map<String, Object> payload) {
        String sessionId = (String) payload.getOrDefault("sessionId", "UNKNOWN_SESSION");
        String warningCode = (String) payload.getOrDefault("ignoredWarningCode", "ISO-GENERIC-OVERRIDE");
        String xml = (String) payload.getOrDefault("forcedXml", "");
        
        // Simular o extraer métricas a string JSON
        Object metricsObj = payload.get("metrics");
        String metricsJson = metricsObj != null ? metricsObj.toString() : "{}";

        ForensicIsoOverrideEntity forensicStamp = new ForensicIsoOverrideEntity(
                userId, sessionId, warningCode, xml, metricsJson
        );

        forensicRepository.save(forensicStamp);

        // Disparo síncrono al SOC On-Premises vía Logback/Syslog
        log.error("[SOC-ALERT] 🛑 ISO 9001 OVERRIDE. El usuario [{}] ignoró deliberadamente la alerta [{}]. Session: {}. Ledger ID: {}", 
                  userId, warningCode, sessionId, forensicStamp.getId());
    }
}
