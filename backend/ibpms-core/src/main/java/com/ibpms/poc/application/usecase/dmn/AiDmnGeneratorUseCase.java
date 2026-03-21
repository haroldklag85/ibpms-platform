package com.ibpms.poc.application.usecase.dmn;

import com.ibpms.poc.application.service.cache.AiDmnCacheService;
import com.ibpms.poc.application.service.security.AiRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Port-In (UseCase) para orquestar la generación DMN de Inteligencia Artificial (SSE).
 * Se encarga de evaluar las barreras SRE (Rate Limit / DoW) y el caché en un hilo no bloqueante.
 */
@Service
public class AiDmnGeneratorUseCase {

    private static final Logger log = LoggerFactory.getLogger(AiDmnGeneratorUseCase.class);

    private final AiRateLimiterService rateLimiterService;
    private final AiDmnCacheService cacheService;
    private final DmnSyntaxGuardUseCase syntaxGuard;
    private final com.ibpms.poc.application.service.dmn.DmnHitPolicyMutatorService policyMutator;

    public AiDmnGeneratorUseCase(AiRateLimiterService rateLimiterService, 
                                 AiDmnCacheService cacheService,
                                 DmnSyntaxGuardUseCase syntaxGuard,
                                 com.ibpms.poc.application.service.dmn.DmnHitPolicyMutatorService policyMutator) {
        this.rateLimiterService = rateLimiterService;
        this.cacheService = cacheService;
        this.syntaxGuard = syntaxGuard;
        this.policyMutator = policyMutator;
    }

    @Async
    public void executeAiStreaming(String userId, String rawPrompt, SseEmitter emitter) {
        log.info("[SRE-SSE] Hilo Asíncrono iniciado. Liberando Tomcat. Rate-Limiting Check para User: {}", userId);

        try {
            // CA-09: Pre-filtro: Recorte bestial de Prompt para no reventar Token Limit
            String prompt = syntaxGuard.validateAndTruncatePrompt(rawPrompt);
            // CA-02: 1. Defensa Denial of Wallet (Rate Limiting)
            if (!rateLimiterService.tryConsumeToken(userId)) {
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"status\": 429, \"message\": \"Rate Limit Excedido (Max 5/min). Cálmese un poco.\"}"));
                emitter.complete();
                return;
            }

            // CA-02: 2. Escudo Criptográfico (Cache SHA-256 Hit)
            String cachedDmn = cacheService.checkCacheHit(prompt);
            if (cachedDmn != null) {
                emitter.send(SseEmitter.event().name("message").data(cachedDmn));
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
                return;
            }

            // CA-01: 3. Streaming NLP-to-DMN (Llamada LLM Simulada para V1)
            // Se asume que en V1.2 aquí va un WebClient iterando líneas de OpenAI.
            String[] mockXmlChunks = {
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
                "<definitions xmlns=\"https://www.omg.org/spec/DMN/20191111/MODEL/\">\n",
                "  <decision id=\"decision_mock\">\n",
                "    <decisionTable id=\"table\" hitPolicy=\"FIRST\"> <!-- Policy FIRST Impuesta -->\n",
                "      <!-- DMN Rule 1 Autogenerada -->\n",
                "    </decisionTable>\n",
                "  </decision>\n",
                "</definitions>"
            };

            StringBuilder finalPayloadAccumulator = new StringBuilder();

            for (String chunk : mockXmlChunks) {
                Thread.sleep(800); // Simulando latencia léxica asíncrona (Evitando Gateway 504)
                
                // Sanitizar saltos de línea para JSON SSE Compatible
                String safeFragment = chunk.replace("\n", ""); 
                emitter.send(SseEmitter.event().name("message").data("{\"fragment\": \"" + safeFragment + "\"}"));
                
                finalPayloadAccumulator.append(chunk);
            }

            // CA-08/CA-09: Post-filtro: Validar el XML Parcial devuelto por IA
            String xmlDevuelto = finalPayloadAccumulator.toString();
            syntaxGuard.validateAiOutputXml(xmlDevuelto);

            // CA-07: Mutator Categórico de Catch-All y HitPolicy FIRST
            String guardedDmnXml = policyMutator.enforceMathGuardrails(xmlDevuelto);

            // 4. Guardado Efímero en Caché usando el DMN Mutado Definitivo
            cacheService.putCache(prompt, guardedDmnXml);
            
            // 5. Señal de Terminación Universal de Streams de IA
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();
            log.info("[SRE-SSE] Data Stream enviado exitosamente. Conexión SSE desenganchada.");

        } catch (Exception e) {
            log.error("[SRE-SSE] Caída crítica en hilo asíncrono AI.", e);
            emitter.completeWithError(e);
        }
    }
}
