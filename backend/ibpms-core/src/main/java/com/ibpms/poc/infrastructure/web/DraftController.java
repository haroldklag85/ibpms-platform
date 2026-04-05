package com.ibpms.poc.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * Endpoints ficticios (Dummies) para I/O CQRS y persistencia de borradores de tareas (US-029, CA-91).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Task Drafts", description = "Endpoints para la gestión CQRS y auto-guardado de Task Drafts")
public class DraftController {

    @Operation(summary = "Persistir borrador", description = "Guarda el borrador parcial del formulario.")
    @PostMapping("/drafts/{taskId}")
    public ResponseEntity<?> saveDraft(@PathVariable String taskId, @RequestBody Object payload) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @Operation(summary = "Recuperar borrador", description = "Reconstruye el borrador de la tarea al reabrir.")
    @GetMapping("/drafts/{taskId}")
    public ResponseEntity<?> getDraft(@PathVariable String taskId) {
        return ResponseEntity.ok(Collections.singletonMap("data", "{}"));
    }

    @Operation(summary = "Limpiar borrador", description = "Limpia el borrador posterior al submit.")
    @DeleteMapping("/drafts/{taskId}")
    public ResponseEntity<?> deleteDraft(@PathVariable String taskId) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @Operation(summary = "Completar Tarea con I/O Mapping", description = "Endpoint dummy para US-029 (CA-91).")
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable String taskId, @RequestBody Object payload) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
