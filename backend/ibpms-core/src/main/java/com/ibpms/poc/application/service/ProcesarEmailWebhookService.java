package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.in.CreateExpedienteUseCase;
import com.ibpms.poc.application.port.in.ProcesarEmailWebhookUseCase;
import com.ibpms.poc.application.port.out.CrmClientPort;
import com.ibpms.poc.application.port.out.NlpAgentPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio Orquestador: M365 NLP => CRM VIP => BPM Camunda.
 * Implementa el Flujo completo del Sprint 3 de Inteligencia.
 */
@Service
public class ProcesarEmailWebhookService implements ProcesarEmailWebhookUseCase {

    private final CrmClientPort crmClientPort;
    private final NlpAgentPort nlpAgentPort;
    private final CreateExpedienteUseCase createExpedienteUseCase;

    public ProcesarEmailWebhookService(CrmClientPort crmClientPort,
            NlpAgentPort nlpAgentPort,
            CreateExpedienteUseCase createExpedienteUseCase) {
        this.crmClientPort = crmClientPort;
        this.nlpAgentPort = nlpAgentPort;
        this.createExpedienteUseCase = createExpedienteUseCase;
    }

    @Override
    @Transactional
    public void procesarEmail(String subject, String body, String senderEmail) {
        // 1. Integración CRM (VIP Status)
        boolean isVip = crmClientPort.checkVipStatus(senderEmail);

        // 2. Integración LLM / AI
        String aiDraft = nlpAgentPort.generateSuggestedReply(body);
        String slaDays = nlpAgentPort.extractLegalSla(body);

        // 3. Serialización de variables para Camunda Native UI
        Map<String, Object> variables = new HashMap<>();
        variables.put("client_vip_status", isVip);
        variables.put("ai_draft_suggested", aiDraft);
        variables.put("calculated_sla_days", slaDays);
        variables.put("email_subject", subject);
        variables.put("sender_email", senderEmail);
        variables.put("email_body_original", body);

        // 4. Lógica de Enrutamiento estático (DMN o Hardcode V1)
        String subjectL = subject != null ? subject.toLowerCase() : "";
        String processKey = (subjectL.contains("demanda") || subjectL.contains("juzgado"))
                ? "Process_Demanda_Legal"
                : "Process_PQR";

        // 5. Invocación a Case Management para persistencia Hexagonal + Motor BPM
        String generatedBusinessKey = "EML-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ExpedienteDTO request = new ExpedienteDTO();
        request.setDefinitionKey(processKey);
        request.setBusinessKey(generatedBusinessKey);
        request.setType("INBOUND_EMAIL");
        request.setVariables(variables);
        request.setIdempotencyKey(UUID.randomUUID().toString()); // Podría ser el Message-ID de Exchange The Graph API

        createExpedienteUseCase.create(request);
    }
}
