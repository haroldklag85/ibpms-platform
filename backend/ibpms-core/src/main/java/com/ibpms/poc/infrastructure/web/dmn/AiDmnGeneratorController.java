package com.ibpms.poc.infrastructure.web.dmn;

import com.ibpms.poc.application.usecase.dmn.AiDmnGeneratorUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Adapter-In REST de Server-Sent Events para Taller DMN. (Hit-The-Canvas). CA-01
 */
@RestController
@RequestMapping("/api/v1/ai/dmn")
public class AiDmnGeneratorController {

    private static final Logger log = LoggerFactory.getLogger(AiDmnGeneratorController.class);
    private final AiDmnGeneratorUseCase aiUseCase;

    public AiDmnGeneratorController(AiDmnGeneratorUseCase aiUseCase) {
        this.aiUseCase = aiUseCase;
    }

    /**
     * Endpoint Generativo. Al producir TEXT_EVENT_STREAM_VALUE el navegador Vue habilita la interfaz nativa EventSource.
     */
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public SseEmitter streamDmnGeneration(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        
        // Timeout de 2 minutos para evitar Zombie Streams en el servidor.
        SseEmitter emitter = new SseEmitter(120000L);
        
        // Extrayendo el Subject ID para gobernar el Bucket4j
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId == null || userId.isBlank()) {
            userId = "ANONYMOUS_RABBIT"; // Escudo nulo
        }

        log.info("[SRE-HTTP] Solicitud streaming recibida de Arquitecto: {}. Delegando al hilo Asíncrono.", userId);
        
        aiUseCase.executeAiStreaming(userId, prompt, emitter);

        // Se devuelve el emisor en el acto (Milisegundos) previniendo el estrangulamiento de Tomcat / Traefik
        return emitter;
    }
}
