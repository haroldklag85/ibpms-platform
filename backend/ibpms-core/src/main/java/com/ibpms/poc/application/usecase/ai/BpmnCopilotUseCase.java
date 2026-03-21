package com.ibpms.poc.application.usecase.ai;

import com.ibpms.poc.application.service.security.AiRateLimiterService;
import com.ibpms.poc.application.service.security.AiJailbreakGuardService;
import com.ibpms.poc.application.service.security.AiPiiAnonymizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * CA-01 / TIN: Port-In (UseCase) para el Agente Generador BPMN.
 * Impone Sovereign Layout (Exclusión de BPMNDi) y estrictas BPMN Constraints.
 */
@Service
public class BpmnCopilotUseCase {

    private static final Logger log = LoggerFactory.getLogger(BpmnCopilotUseCase.class);

    private final AiRateLimiterService rateLimiterService;
    private final BpmnLayoutAdapter layoutAdapter;
    private final AiJailbreakGuardService guardService;
    private final AiPiiAnonymizerService piiService;
    private final RagSessionCleanerUseCase sessionCleaner;

    // Prompt estricto quemado en código - Prevención de Inyección y Geometría
    private static final String SYSTEM_PROMPT = """
        Eres un Copiloto Arquitecto BPMN 2.0 estricto y de confianza nula.
        REGLA 1 (SOVEREIGN LAYOUT Y RESTRICCIONES CATEGOÓRICAS): 
          - Genera SOLO el bloque <bpmn:process>. OMITE POR COMPLETO el bloque visual <bpmndi:BPMNDiagram>. Jamás calcules coordenadas (X,Y).
          - Prohibido el uso de <callActivity>, <subProcess> o Eventos Complejos. 
          - Nodos permitidos: startEvent, endEvent, userTask, serviceTask, exclusiveGateway, boundaryEvent (error_only).
        REGLA 2 (TRADUCCIÓN ACTIVA):
          - El XML generado (nombres de tareas, eventos, IDs descriptivos) DEBE emitirse incondicionalmente en ESPAÑOL, sin importar el idioma de origen del documento procesado vía RAG.
        REGLA 3 (PREVENCIÓN DE BUCLES REDUNDANTES):
          - Jamás dupliques nodos secuenciales para repetir pasos lógicos. Dibuja un SequenceFlow invertido apuntando al nodo inicial del ciclo.
        REGLA 4 (SANEAMIENTO DE GATEWAYS Y ROLES):
          - Los Gateways exclusivos nacerán VACÍOS. JAMÁS inyectes sintaxis matemática o scripts FEEL en su interior. La lógica de evaluación recae en variables dummy fuera del BPMN.
          - Si el documento fuerza un Rol Huérfano que no encaja operativamente, inyecta un <bpmn:textAnnotation> contiguo alertando al Arquitecto Humano.
        REGLA 5 (TRIAGE CONVERSACIONAL Y PARADA DE EMERGENCIA):
          - Si el análisis encuentra contradicciones procedimentales o variables imposibles de resolver, ABORTA la generación XML y emite JSON/Texto formulando un máximo de 3 Preguntas Cortas con 'Action Pills' (Opciones sugeridas) para que el humano rompa el empate antes de reanudar el flujo generativo.
        REGLA 6 (OUTPUT FORMAT): 
          - Si no hay ambigüedad, retorna el XML crudo sin envoltura Markdown. Si hay ambigüedad (Triage), retorna JSON {"type":"triage", "questions":[{"text":"?", "options":["A","B"]}]}.
        """;

    public BpmnCopilotUseCase(AiRateLimiterService rateLimiterService, 
                              BpmnLayoutAdapter layoutAdapter,
                              AiJailbreakGuardService guardService,
                              AiPiiAnonymizerService piiService,
                              RagSessionCleanerUseCase sessionCleaner) {
        this.rateLimiterService = rateLimiterService;
        this.layoutAdapter = layoutAdapter;
        this.guardService = guardService;
        this.piiService = piiService;
        this.sessionCleaner = sessionCleaner;
    }

    /**
     * CA-04: Transfiere la Orden de Purga de Vectores al UseCase Destructivo.
     */
    public void triggerRagSessionWipe(String tenantId, String sessionId) {
        sessionCleaner.wipeSessionFootprint(tenantId, sessionId);
    }

    @Async
    public void executeBpmnGenerationStream(String userId, String humanPrompt, SseEmitter emitter) {
        log.info("[SRE-COPILOT] Hilo Invocado. Verificando tokens para Usuario: {}", userId);

        try {
            // Rate Limiting (CA-01)
            if (!rateLimiterService.tryConsumeToken(userId)) {
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"status\": 429, \"message\": \"Rate Limit Copilot Excedido.\"}"));
                emitter.complete();
                return;
            }

            // CA-05: Escudo 3-Strikes Anti-Jailbreak Kill Switch
            guardService.inspectAndEnforce(userId, humanPrompt);

            // CA-05: Enmascarado Riguroso PII
            String maskedPrompt = piiService.maskSensitiveData(humanPrompt);

            // Truncamiento Severo de Tokens (1000 Chars Humanos + System Prompt)
            String safePrompt = maskedPrompt != null && maskedPrompt.length() > 1000 
                ? maskedPrompt.substring(0, 1000) : maskedPrompt;
                
            log.debug("[SRE-COPILOT] Prompt ensamblado con Segregación Topológica y PII Enmascarado. {}... [System Rule Inyectada]", safePrompt.substring(0, Math.min(20, safePrompt.length())));
            log.trace("[SRE-COPILOT-PROMPT] Reglas Subyacentes: {}", SYSTEM_PROMPT);

            // Streaming Mock Asíncrono
            String[] mockXmlChunks = {
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n",
                "  <bpmn:process id=\"Process_AI_Gen\" isExecutable=\"true\">\n",
                "    <bpmn:startEvent id=\"Start_1\" />\n",
                "    <bpmn:userTask id=\"Task_1\" name=\"Revisar Documento\" />\n",
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"Start_1\" targetRef=\"Task_1\" />\n",
                "    <bpmn:endEvent id=\"End_1\" />\n",
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"Task_1\" targetRef=\"End_1\" />\n",
                "  </bpmn:process>\n",
                "</bpmn:definitions>"
            };

            StringBuilder semanticXmlAccumulator = new StringBuilder();

            for (String chunk : mockXmlChunks) {
                Thread.sleep(600); // Simulando red OpenAI / Anthropic
                // Transmitiendo el XML lógico en bruto al Front (Sovereign Semantic Streaming)
                String safeFragment = chunk.replace("\n", ""); 
                emitter.send(SseEmitter.event().name("message").data("{\"fragment\": \"" + safeFragment + "\"}"));
                semanticXmlAccumulator.append(chunk);
            }

            // Post-Procesamiento Matemático: Inyectar DI auto-generado antes del cierre
            String xmlWithLayout = layoutAdapter.injectMathematicalTopology(semanticXmlAccumulator.toString());
            
            // Enviamos el XML Definitivo al LocalStorage del Frontend para que lo pinte
            String safeFinal = xmlWithLayout.replace("\n", "").replace("\"", "\\\"");
            emitter.send(SseEmitter.event().name("layout_resolved").data("{\"fullXml\": \"" + safeFinal + "\"}"));

            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();

        } catch (Exception e) {
            log.error("[SRE-COPILOT] Falla abrupta en hilo de SSE.", e);
            emitter.completeWithError(e);
        }
    }
}
