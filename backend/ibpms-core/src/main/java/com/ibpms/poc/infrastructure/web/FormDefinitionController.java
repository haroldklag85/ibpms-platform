package com.ibpms.poc.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

/**
 * Endpoint para gestión inmutable de versiones JSONB de Formularios (US-003, CA-87).
 */
@RestController
@RequestMapping("/api/v1/forms")
@Tag(name = "Form Definitions", description = "Endpoints para la gestión de versiones inmutables del esquema JSONB de formularios")
public class FormDefinitionController {

    @Operation(summary = "Crear nueva versión de diseño", description = "Persiste el AST del formulario en formato JSONB inmutable (v1, v2, etc).")
    @PostMapping("/{formId}")
    public ResponseEntity<?> saveFormVersion(@PathVariable UUID formId, @RequestBody String schemaContent) {
        // Dummy funcional: Simula éxito transaccional
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @Operation(summary = "Listar versiones de formulario", description = "Retorna el historial de diseño para permitir Rollbacks (Audit).")
    @GetMapping("/{formId}/versions")
    public ResponseEntity<?> getFormVersions(@PathVariable UUID formId) {
        // Dummy funcional
        return ResponseEntity.ok(Collections.emptyList());
    }
}
