package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.FormSchemaDTO;
import com.ibpms.poc.application.dto.TaskDTO;
import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.in.ListarTareasUseCase;
import com.ibpms.poc.application.port.in.ObtenerFormularioUseCase;
import com.ibpms.poc.application.port.in.ReclamarTareaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Adaptador Driving — Controlador REST de Tareas y Formularios Dinámicos.
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final ListarTareasUseCase listarTareasUseCase;
    private final CompletarTareaUseCase completarTareaUseCase;
    private final ObtenerFormularioUseCase obtenerFormularioUseCase;
    private final ReclamarTareaUseCase reclamarTareaUseCase;

    public TaskController(ListarTareasUseCase listarTareasUseCase,
            CompletarTareaUseCase completarTareaUseCase,
            ObtenerFormularioUseCase obtenerFormularioUseCase,
            ReclamarTareaUseCase reclamarTareaUseCase) {
        this.listarTareasUseCase = listarTareasUseCase;
        this.completarTareaUseCase = completarTareaUseCase;
        this.obtenerFormularioUseCase = obtenerFormularioUseCase;
        this.reclamarTareaUseCase = reclamarTareaUseCase;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> listTasks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(required = false) Integer priority) {

        List<TaskDTO> result = listarTareasUseCase.listar(limit, offset, status, priority);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{taskId}/form")
    public ResponseEntity<FormSchemaDTO> getTaskForm(@PathVariable String taskId) {
        FormSchemaDTO formJson = obtenerFormularioUseCase.obtenerFormulario(taskId);
        return ResponseEntity.ok(formJson);
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> payload) {

        // El payload esperado, según openapi.yaml, tiene la estructura {"variables":
        // {...}}
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload.containsKey("variables")
                ? (Map<String, Object>) payload.get("variables")
                : payload;

        completarTareaUseCase.completar(taskId, variables, idempotencyKey);

        // Endpoint que retorna HTTP 204 No Content
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Void> claimTask(@PathVariable String taskId) {
        String username;
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            username = "maria.lopez"; // Mock default para entorno sin Security context activo
        }

        reclamarTareaUseCase.reclamar(taskId, username);
        return ResponseEntity.ok().build();
    }
}
