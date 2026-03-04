package com.ibpms.poc.api.controller.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoint de gestión para la "Librería de Prompts" (Prompt Library).
 * Aquí se configuran las instrucciones maestras (System Prompts) para los
 * modelos LLM.
 * Solo roles autorizados pueden alterar las instrucciones Core.
 */
@RestController
@RequestMapping("/api/v1/prompts")
public class PromptLibraryController {

    /**
     * Recupera un Prompt. Acceso público de solo lectura (Cualquier usuario
     * autenticado).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getPrompt(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("id", id.toString(), "content", "Eres un asistente IA experto en iBPMS..."));
    }

    /**
     * Endpoint crítico para modificación estructural.
     * EXCLUSIVO para ingenieros de prompts. (QA: Retorna 403 Forbidden si no tiene
     * el rol).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('prompt_engineer')")
    public ResponseEntity<String> updateMasterPrompt(@PathVariable UUID id, @RequestBody String newContent) {
        // Lógica de guardado en base de datos
        return ResponseEntity.ok("Prompt maestro actualizado exitosamente en BDD bajo parámetros de Rol protegido.");
    }
}
