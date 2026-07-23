package com.ibpms.poc.infrastructure.web.bpm;

import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de Bypass Anónimo (CA-15).
 * Permite iniciar procesos sin autenticación si están marcados como isPublic.
 */
@RestController
@RequestMapping("/api/v1/process")
public class AnonymousProcessController {

    private static final Logger log = LoggerFactory.getLogger(AnonymousProcessController.class);
    private final RuntimeService runtimeService;
    private final BpmnProcessDesignRepository processDesignRepository;

    public AnonymousProcessController(RuntimeService runtimeService, BpmnProcessDesignRepository processDesignRepository) {
        this.runtimeService = runtimeService;
        this.processDesignRepository = processDesignRepository;
    }

    @PostMapping("/{key}/start-anonymous")
    public ResponseEntity<Map<String, Object>> startAnonymousProcess(
            @PathVariable String key, 
            @RequestBody Map<String, Object> variables) {
        
        log.info("CA-15: Intento de inicio anónimo para proceso [{}]", key);

        // 1. Validar si el proceso existe y es público
        BpmnProcessDesignEntity design = processDesignRepository.findByTechnicalId(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Definición de proceso no encontrada."));

        if (!design.isPublic()) {
            log.warn("CA-15: Intento RECHAZADO. El proceso [{}] no está marcado como público.", key);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este trámite requiere autenticación obligatoria.");
        }

        // 2. Inyectar trazabilidad segura
        variables.put("ibpms_initiator_type", "ANONYMOUS_GUEST");
        variables.put("ibpms_initiator_id", "public-web-form");

        var instance = runtimeService.startProcessInstanceByKey(key, variables);
        
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("status", "STARTED_ANONYMOUSLY");
        
        return ResponseEntity.ok(response);
    }
}
