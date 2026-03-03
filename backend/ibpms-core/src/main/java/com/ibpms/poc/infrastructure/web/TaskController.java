package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.FormSchemaDTO;
import com.ibpms.poc.application.dto.TaskDTO;
import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.in.LiberarTareaUseCase;
import com.ibpms.poc.application.port.in.ListarTareasUseCase;
import com.ibpms.poc.application.port.in.ObtenerFormularioUseCase;
import com.ibpms.poc.application.port.in.ReasignarTareaUseCase;
import com.ibpms.poc.application.port.in.ReclamarTareaUseCase;
import com.ibpms.poc.application.service.FormFieldCleanserService;
import com.ibpms.poc.infrastructure.jpa.entity.FormFieldValueAuditEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormFieldValueAuditRepository;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final LiberarTareaUseCase liberarTareaUseCase;
    private final ReasignarTareaUseCase reasignarTareaUseCase;
    private final FormFieldCleanserService formFieldCleanserService;
    private final TaskService taskService;
    private final FormFieldValueAuditRepository auditRepository;

    public TaskController(ListarTareasUseCase listarTareasUseCase,
            CompletarTareaUseCase completarTareaUseCase,
            ObtenerFormularioUseCase obtenerFormularioUseCase,
            ReclamarTareaUseCase reclamarTareaUseCase,
            LiberarTareaUseCase liberarTareaUseCase,
            ReasignarTareaUseCase reasignarTareaUseCase,
            FormFieldCleanserService formFieldCleanserService,
            TaskService taskService,
            FormFieldValueAuditRepository auditRepository) {
        this.listarTareasUseCase = listarTareasUseCase;
        this.completarTareaUseCase = completarTareaUseCase;
        this.obtenerFormularioUseCase = obtenerFormularioUseCase;
        this.reclamarTareaUseCase = reclamarTareaUseCase;
        this.liberarTareaUseCase = liberarTareaUseCase;
        this.reasignarTareaUseCase = reasignarTareaUseCase;
        this.formFieldCleanserService = formFieldCleanserService;
        this.taskService = taskService;
        this.auditRepository = auditRepository;
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

    @PutMapping("/{taskId}/draft")
    public ResponseEntity<Void> autoSaveDraft(@PathVariable String taskId, @RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload.containsKey("variables")
                ? (Map<String, Object>) payload.get("variables")
                : payload;

        formFieldCleanserService.cleanseVariables(variables);
        taskService.setVariablesLocal(taskId, variables);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> payload) {

        @SuppressWarnings("unchecked")
        Map<String, Object> variables = payload.containsKey("variables")
                ? (Map<String, Object>) payload.get("variables")
                : payload;

        // 1. Limpieza de máscaras estéticas (CA-31)
        formFieldCleanserService.cleanseVariables(variables);

        // 2. Auditoría CA-12: Capturar el delta de cambios
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            String processInstanceId = task.getProcessInstanceId();
            Map<String, Object> oldVariables = taskService.getVariables(taskId);
            String username = SecurityContextHolder.getContext() != null
                    && SecurityContextHolder.getContext().getAuthentication() != null
                            ? SecurityContextHolder.getContext().getAuthentication().getName()
                            : "system";

            variables.forEach((key, newValue) -> {
                Object oldValue = oldVariables.get(key);
                String oldStr = oldValue != null ? oldValue.toString() : null;
                String newStr = newValue != null ? newValue.toString() : null;

                if (oldStr == null || !oldStr.equals(newStr)) {
                    UUID formId = new UUID(0, 0); // Mock UUID for form design ID since it involves joining with
                                                  // Definition
                    auditRepository.save(
                            new FormFieldValueAuditEntity(processInstanceId, formId, key, oldStr, newStr, username));
                }
            });
        }

        // 3. Completar y despachar engine
        completarTareaUseCase.completar(taskId, variables, idempotencyKey);

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

    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<Void> unclaimTask(@PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> payload) {
        String username;
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            username = "maria.lopez";
        }

        String reason = null;
        Map<String, Object> variables = null;
        if (payload != null) {
            reason = (String) payload.get("reason");

            // Supresión temporal: Safe cast
            @SuppressWarnings("unchecked")
            Map<String, Object> v = payload.containsKey("variables")
                    ? (Map<String, Object>) payload.get("variables")
                    : payload;

            variables = v;
            if (variables.containsKey("reason")) {
                variables.remove("reason");
            }
        }

        liberarTareaUseCase.liberar(taskId, username, variables, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/reassign")
    public ResponseEntity<Void> reassignTask(@PathVariable String taskId, @RequestBody Map<String, Object> payload) {
        String currentUsername;
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            currentUsername = "maria.lopez";
        }

        String newUserId = (String) payload.get("newUserId");
        String reason = (String) payload.get("reason");

        if (newUserId == null || newUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'newUserId' es obligatorio para reasignar una tarea.");
        }

        reasignarTareaUseCase.reasignar(taskId, currentUsername, newUserId, reason);
        return ResponseEntity.ok().build();
    }
}
