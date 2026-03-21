package com.ibpms.poc.infrastructure.web.ai;

import com.ibpms.poc.application.usecase.ai.AiFormGeneratorUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CA-73: Controlador Edge para la Auto-Generación AI de Formularios (Escáner Mágico).
 */
@RestController
@RequestMapping("/api/v1/design/forms/generate")
public class AiFormGeneratorController {

    private final AiFormGeneratorUseCase aiFormGeneratorUseCase;

    public AiFormGeneratorController(AiFormGeneratorUseCase aiFormGeneratorUseCase) {
        this.aiFormGeneratorUseCase = aiFormGeneratorUseCase;
    }

    /**
     * Consume el Prompt del Arquitecto Maestro y devuelve un Árbol Zod/Vue puro.
     */
    @PostMapping
    @PreAuthorize("hasRole('PROCESS_ARCHITECT')")
    public ResponseEntity<String> generateForm(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Empty promptly disallowed.\"}");
        }

        // El UseCase garantiza devolver un string JSON validable
        String jsonResult = aiFormGeneratorUseCase.generateFormJson(prompt);
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(jsonResult);
    }
}
