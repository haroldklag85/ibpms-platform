package com.ibpms.poc.infrastructure.web.bff;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.application.service.bff.FormBffCoreService;
import java.util.Map;

/**
 * Misión Cero: Scaffold de Controlador Crítico para estabilizar Tomcat 8080.
 * US-029 (BFF Form-Context).
 */
@RestController
@RequestMapping("/api/v1/workbox/tasks")
public class FormBffController {

    private final FormBffCoreService bffCoreService;

    @Autowired
    public FormBffController(FormBffCoreService bffCoreService) {
        this.bffCoreService = bffCoreService;
    }

    @GetMapping("/{id}/form-context")
    public ResponseEntity<?> getFormContext(
            @PathVariable("id") String taskId,
            @RequestHeader(value = "X-User-Id", defaultValue = "user") String userId) {
        
        return ResponseEntity.ok(bffCoreService.generateMegaDtoFormContext(taskId, userId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable("id") String taskId, 
            @RequestHeader(value = "X-User-Id", defaultValue = "user") String userId,
            @RequestBody Map<String, Object> payload) {
        
        bffCoreService.completeTransactionalForm(taskId, userId, payload);
        return ResponseEntity.ok(Map.of("message", "Guardado exitoso CQRS (Event Sourcing)"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAssigneeCollision(AccessDeniedException ex) {
        // CA-07: Retorna 403 sí la identidad colisiona
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleEngineCrash(IllegalStateException ex) {
        // CA-10: Error Crudo 500 Motor no disponible. Rollback CQRS Sagas
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", "Transacción Abortada: " + ex.getMessage()));
    }
}
