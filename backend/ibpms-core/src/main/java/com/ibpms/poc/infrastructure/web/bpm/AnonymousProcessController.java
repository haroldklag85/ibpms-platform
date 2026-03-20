package com.ibpms.poc.infrastructure.web.bpm;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de Bypass Anónimo (CA-15).
 * Spring Security permitirá el tráfico libre a estas rutas.
 */
@RestController
@RequestMapping("/api/v1/process")
public class AnonymousProcessController {

    private final RuntimeService runtimeService;
    // Injectar ProcessVisibilityRepository aqui o un servicio intermedio si existiese

    public AnonymousProcessController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/{key}/start-anonymous")
    public ResponseEntity<Map<String, Object>> startAnonymousProcess(@PathVariable String key, @RequestBody Map<String, Object> variables) {
        
        // Simulación: Validación de la bandera isPublic == true en BD local o API
        boolean isPublic = true; // TODO: Reemplazar por lookup real (ej. visibilityRepository.isProcessPublic(key))
        if (!isPublic) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Inyectar trazabilidad segura
        variables.put("ibpms_initiator_type", "ANONYMOUS_GUEST");
        variables.put("ibpms_initiator_id", "public-web-form");

        var instance = runtimeService.startProcessInstanceByKey(key, variables);
        
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("status", "STARTED");
        
        return ResponseEntity.ok(response);
    }
}
