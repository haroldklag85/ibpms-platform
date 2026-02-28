package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.in.CreateExpedienteUseCase;
import com.ibpms.poc.application.port.in.ProcesarEmailWebhookUseCase;
import com.ibpms.poc.application.port.out.CrmClientPort;
import com.ibpms.dmn.NlpAgentPort;
import com.ibpms.poc.application.port.out.AiAuditLedgerPort;
import com.ibpms.poc.application.port.out.VectorDatabasePort;
import com.ibpms.poc.application.port.out.VectorDatabasePort.KnowledgeMatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio Orquestador: M365 NLP => CRM VIP => BPM Camunda.
 * Incorpora RAG (Recuperación de conocimiento en Vectores) y eDiscovery.
 */
@Service
public class ProcesarEmailWebhookService implements ProcesarEmailWebhookUseCase {

    private final CrmClientPort crmClientPort;
    private final NlpAgentPort nlpAgentPort;
    private final CreateExpedienteUseCase createExpedienteUseCase;
    private final VectorDatabasePort vectorDbPort;
    private final AiAuditLedgerPort aiAuditLog;

    public ProcesarEmailWebhookService(CrmClientPort crmClientPort,
            NlpAgentPort nlpAgentPort,
            CreateExpedienteUseCase createExpedienteUseCase,
            VectorDatabasePort vectorDbPort,
            AiAuditLedgerPort aiAuditLog) {
        this.crmClientPort = crmClientPort;
        this.nlpAgentPort = nlpAgentPort;
        this.createExpedienteUseCase = createExpedienteUseCase;
        this.vectorDbPort = vectorDbPort;
        this.aiAuditLog = aiAuditLog;
    }

    @Override
    @Transactional
    public void procesarEmail(String subject, String body, String senderEmail) {
        // 1. Integración CRM (VIP Status)
        boolean isVip = crmClientPort.checkVipStatus(senderEmail);

        // 2. RAG: Buscar contexto similar en la BD Vectorial
        List<Double> currentEmbeddings = nlpAgentPort.generateEmbeddings(body);
        List<KnowledgeMatch> similarCases = vectorDbPort.searchSimilarPastResponses(currentEmbeddings, 3);

        String contextExtracted = ""; // Compilar el contexto si hay *matches*.
        if (!similarCases.isEmpty()) {
            contextExtracted = "\n\n=== CONTEXTO HISTÓRICO PARA RAG (Aprende de estos ejemplos humanos y moldea tu respuesta basándote en ellos) ===\n"
                    +
                    similarCases.stream()
                            .map(k -> "Mail Original: " + k.getContext() + "\nResp. Aprobada Humana: "
                                    + k.getHumanApprovedReply())
                            .collect(Collectors.joining("\n---\n"));
        }

        // 3. Integración LLM / AI (Inyectando RAG)
        String raggifiedPrompt = body + contextExtracted;
        String aiDraft = nlpAgentPort.generateSuggestedReply(raggifiedPrompt);
        String slaDays = nlpAgentPort.extractLegalSla(body);

        // 4. Invocación a Case Management para persistencia Hexagonal + Motor BPM
        String generatedBusinessKey = "EML-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5. eDiscovery (Audit Log inmutable de la decisión del LLM vs Petición
        // original)
        aiAuditLog.logAiSuggestion(generatedBusinessKey, "gpt-4o-mini / RAG Top-3", "Generar Borrador Atención Cliente",
                aiDraft);

        // 6. Serialización de variables para Camunda Native UI
        Map<String, Object> variables = new HashMap<>();
        variables.put("client_vip_status", isVip);
        variables.put("ai_draft_suggested", aiDraft);
        variables.put("calculated_sla_days", slaDays);
        variables.put("email_subject", subject);
        variables.put("sender_email", senderEmail);
        variables.put("email_body_original", body);

        // 7. Lógica de Enrutamiento estático (DMN o Hardcode V1)
        String subjectL = subject != null ? subject.toLowerCase() : "";
        String processKey = (subjectL.contains("demanda") || subjectL.contains("juzgado"))
                ? "Process_Demanda_Legal"
                : "Process_PQR";

        ExpedienteDTO request = new ExpedienteDTO();
        request.setDefinitionKey(processKey);
        request.setBusinessKey(generatedBusinessKey);
        request.setType("INBOUND_EMAIL");
        request.setVariables(variables);
        request.setIdempotencyKey(UUID.randomUUID().toString()); // Podría ser el Message-ID de Exchange The Graph API

        createExpedienteUseCase.create(request);
    }
}
