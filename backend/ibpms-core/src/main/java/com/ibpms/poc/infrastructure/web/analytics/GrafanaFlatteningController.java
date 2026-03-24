package com.ibpms.poc.infrastructure.web.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Misión Cero: Persistencia Flattening para Grafana (US-039).
 */
@RestController
@RequestMapping("/api/v1/grafana")
public class GrafanaFlatteningController {

    @PostMapping("/flatten")
    public ResponseEntity<?> triggerFlatteningPipeline() {
        return ResponseEntity.ok(Map.of("message", "Grafana Flattening Job Invocado"));
    }
}
