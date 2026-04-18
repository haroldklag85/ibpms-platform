package com.ibpms.poc.infrastructure.web.ai;

import com.ibpms.poc.application.usecase.ai.BpmnCopilotUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * CA-01 / TIN: Adapter-In REST para el Copiloto IA (Diseñador BPMN).
 */
@Tag(name = "AI BPMN Copilot", description = "Copiloto Generador de flujos BPMN asíncrono")
@RestController
@RequestMapping("/api/v1/ai/copilot")
public class BpmnCopilotController {

    private final BpmnCopilotUseCase copilotUseCase;

    public BpmnCopilotController(BpmnCopilotUseCase copilotUseCase) {
        this.copilotUseCase = copilotUseCase;
    }

    /**
     * Endpoint Generativo de BPMN usando Server-Sent Events (Asíncrono Anti-Gateway Timeout).
     */
    @Operation(
            summary = "Streaming Generativo de BPMN",
            description = "Emite eventos Layout/XML BPMN en tiempo real mediante SseEmitter, evitando interrupciones HTTP.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Flujo binario asíncrono (Event-Stream).",
                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    schema = @Schema(type = "string", format = "binary")))
            }
    )
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER')")
    public SseEmitter streamBpmnGeneration(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        
        // Timeout SRE - 3 Minutos máximo de espera de IA
        SseEmitter emitter = new SseEmitter(180000L);
        
        // TIN Constraint: Extrayendo el Subject ID para el Rate Limiter (Denial of Wallet)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId == null || userId.isBlank()) {
            userId = "ANONYMOUS_ARCHITECT"; 
        }

        // Delegación de Hilo para liberar API Gateway
        copilotUseCase.executeBpmnGenerationStream(userId, prompt, emitter);

        return emitter;
    }

    /**
     * CA-04: Destructor Efímero (End of Session RAG Boundary).
     * @param sessionId token correlativo desde el Frontend para eliminar vectores.
     */
    @DeleteMapping("/session")
    @PreAuthorize("hasAnyAuthority('ROLE_PROCESS_ARCHITECT', 'ROLE_BPMN_DESIGNER')")
    public ResponseEntity<Void> wipeCopilotMemory(@RequestParam String sessionId) {
        // En V1 extraemos Tenant_ID asumiendo un Auth Context Mockeado
        String tenantId = "tenant_hq_corp"; 
        
        copilotUseCase.triggerRagSessionWipe(tenantId, sessionId);
        return ResponseEntity.ok().build();
    }
}
