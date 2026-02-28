package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.AiAuditLedgerPort;
import com.ibpms.poc.infrastructure.jpa.entity.AiAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.AiAuditLogRepository;
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
        log.setBusinessKey(businessKey);
        log.setEventType("AI_DRAFT_SUGGESTED");
        log.setAiModelVersion(aiModel);
        log.setAppliedPrompt(systemPrompt);
        log.setContentPayload(suggestedText);

        repository.save(log);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logHumanResolution(String businessKey, String operatorId, String finalSentText,
            double similarityScore) {
        AiAuditLogEntity log = new AiAuditLogEntity();
        log.setBusinessKey(businessKey);
        log.setEventType("HUMAN_APPROVED");
        log.setOperatorId(operatorId);
        log.setContentPayload(finalSentText);
        log.setSimilarityScore(similarityScore);

        repository.save(log);
    }
}
