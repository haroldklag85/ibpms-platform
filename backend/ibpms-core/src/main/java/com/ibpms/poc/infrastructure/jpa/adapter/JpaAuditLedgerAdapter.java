package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.AiAuditLedgerPort;
import com.ibpms.poc.infrastructure.jpa.entity.ai.AiAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ai.AiAuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mapeador e implementación de Capa Anti-Corrupción para Audit Ledger
 * (eDiscovery).
 * Se encarga de guardar las evidencias de AI y Humanos en Base de Datos de
 * forma inmutable.
 */
@Component
public class JpaAuditLedgerAdapter implements AiAuditLedgerPort {

    private final AiAuditLogRepository repository;

    public JpaAuditLedgerAdapter(AiAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Garantiza guardado inmutable aunque el proceso principal
                                                           // falle (Audit)
    public void logAiSuggestion(String businessKey, String aiModel, String systemPrompt, String suggestedText) {
        AiAuditLogEntity log = new AiAuditLogEntity();
        log.setExecutionId(businessKey); // Usado para referenciar el caso de negocio
        log.setSystemPrompt(systemPrompt);
        log.setResponsePayload(suggestedText);
        log.setChainOfThought("AI Model: " + aiModel + " | Event: AI_DRAFT_SUGGESTED");

        repository.save(log);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logHumanResolution(String businessKey, String operatorId, String finalSentText,
            double similarityScore) {
        AiAuditLogEntity log = new AiAuditLogEntity();
        log.setExecutionId(businessKey);
        log.setHumanOverride("Operator: " + operatorId + " | Event: HUMAN_APPROVED");
        log.setResponsePayload(finalSentText);
        log.setConfidenceScore(similarityScore);

        repository.save(log);
    }
}
