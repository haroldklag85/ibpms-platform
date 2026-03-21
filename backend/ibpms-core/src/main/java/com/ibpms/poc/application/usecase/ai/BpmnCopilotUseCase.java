package com.ibpms.poc.application.usecase.ai;

import com.ibpms.poc.application.service.security.AiRateLimiterService;
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

    // Prompt estricto quemado en código - Prevención de Inyección y Geometría
    private static final String SYSTEM_PROMPT = """
        Eres un Copiloto Arquitecto BPMN 2.0 estricto.
        REGLA 1 (SOVEREIGN LAYOUT): Genera SOLO el bloque <bpmn:process>. OMITE POR COMPLETO el bloque visual <bpmndi:BPMNDiagram>. Jamás calcules coordenadas (X,Y).
        REGLA 2 (CONSTRAINTS): Tienes PROHIBIDO usar <callActivity>, <subProcess> o Eventos Complejos. 
        Usa ÚNICAMENTE: startEvent, endEvent, userTask, serviceTask, exclusiveGateway, boundaryEvent (error).
        REGLA 3 (OUTPUT): Retorna el XML crudo sin markdown, sin explicaciones.
        """;

    public BpmnCopilotUseCase(AiRateLimiterService rateLimiterService, BpmnLayoutAdapter layoutAdapter) {
        this.rateLimiterService = rateLimiterService;
        this.layoutAdapter = layoutAdapter;
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

            // Truncamiento Severo de Tokens (1000 Chars Humanos + System Prompt)
            String safePrompt = humanPrompt != null && humanPrompt.length() > 1000 
                ? humanPrompt.substring(0, 1000) : humanPrompt;
                
            log.debug("[SRE-COPILOT] Prompt ensamblado con Segregación Topológica. Iniciando Fake LLM Stream...");

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
