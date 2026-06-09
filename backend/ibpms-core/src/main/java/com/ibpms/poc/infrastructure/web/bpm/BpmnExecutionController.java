package com.ibpms.poc.infrastructure.web.bpm;

import com.ibpms.poc.application.dto.StartProcessRequest;
import com.ibpms.poc.application.dto.StartProcessResult;
import com.ibpms.poc.application.port.in.CompletarTareaUseCase;
import com.ibpms.poc.application.port.in.StartProcessUseCase;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST de Ejecución BPMN — Punto de entrada autenticado.
 * Expone operaciones de runtime del motor BPMN (iniciar proceso, completar tarea)
 * siguiendo los contratos definidos en API_CONTRACTS.md (Sección 5.4).
 *
 * <p>Arquitectura Hexagonal: Este controller NUNCA importa clases de Camunda.
 * Toda interacción se realiza a través de los puertos de entrada
 * ({@link StartProcessUseCase}, {@link CompletarTareaUseCase}).
 *
 * @Traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal), ADR-003 (Camunda Embedded)
 */
@RestController
@RequestMapping("/api/bpmn")
@Slf4j
public class BpmnExecutionController {

    private final StartProcessUseCase startProcessUseCase;
    private final CompletarTareaUseCase completarTareaUseCase;

    public BpmnExecutionController(StartProcessUseCase startProcessUseCase,
                                   CompletarTareaUseCase completarTareaUseCase) {
        this.startProcessUseCase = startProcessUseCase;
        this.completarTareaUseCase = completarTareaUseCase;
    }

    /**
     * Inicia una nueva instancia de proceso BPMN.
     * Contrato: POST /api/bpmn/instances (API_CONTRACTS.md L568).
     *
     * @param request Payload con processDefinitionKey, businessKey y variables
     * @return 201 Created con los datos de la instancia creada
     */
    @PostMapping("/instances")
    public ResponseEntity<StartProcessResult> startProcessInstance(
            @Valid @RequestBody StartProcessRequest request) {

        String username = resolveAuthenticatedUsername();

        log.info("POST /api/bpmn/instances — Inicio de proceso [key={}, user={}]",
                request.processDefinitionKey(), username);

        StartProcessResult result = startProcessUseCase.start(
                request.processDefinitionKey(),
                request.businessKey(),
                request.variables() != null ? request.variables() : Map.of(),
                username);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Completa una tarea de usuario (UserTask) adjuntando variables de formulario.
     * Delega al {@link CompletarTareaUseCase} existente (reutilización hexagonal).
     *
     * @param taskId         ID de la tarea Camunda
     * @param idempotencyKey Clave para prevenir doble submit (opcional)
     * @param variables      Variables del formulario completado
     * @return 204 No Content tras completar exitosamente
     */
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) Map<String, Object> variables) {

        String username = resolveAuthenticatedUsername();

        log.info("POST /api/bpmn/tasks/{}/complete — Completar tarea [user={}]",
                taskId, username);

        completarTareaUseCase.completar(
                taskId,
                variables != null ? variables : Map.of(),
                idempotencyKey,
                username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el username del contexto de seguridad de Spring.
     * Fallback a "system" si no hay autenticación activa (perfil dev).
     */
    private String resolveAuthenticatedUsername() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getName() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }
}
