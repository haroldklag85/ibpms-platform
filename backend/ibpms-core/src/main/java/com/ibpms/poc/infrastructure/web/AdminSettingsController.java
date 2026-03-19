package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de Ajustes Administrativos Generales.
 * Provee umbrales y parametrizaciones del sistema (CA-30).
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminSettingsController {

    /**
     * CA-30: Configurador de Umbrales de Complejidad BPMN.
     * En el futuro, el valor '100' vendrá de un Vault o Base de Datos.
     */
    @GetMapping("/bpmn-complexity-limit")
    public ResponseEntity<Map<String, Integer>> getBpmnComplexityLimit() {
        return ResponseEntity.ok(Map.of("nodeLimit", 100));
    }
}
