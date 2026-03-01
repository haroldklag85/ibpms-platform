package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.AiMetricsDTO;
import com.ibpms.poc.application.dto.ProcessHealthDTO;
import com.ibpms.poc.application.port.in.ObtenerMetricasUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para Dashboards BAM y Analítica de Inteligencia Artificial (Bloque
 * C)
 */
@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasRole('Directivo') or hasRole('Admin_Intake')")
public class AnalyticsController {

    private final ObtenerMetricasUseCase obtenerMetricasUseCase;

    public AnalyticsController(ObtenerMetricasUseCase obtenerMetricasUseCase) {
        this.obtenerMetricasUseCase = obtenerMetricasUseCase;
    }

    @GetMapping("/process-health")
    public ResponseEntity<ProcessHealthDTO> getProcessHealth() {
        return ResponseEntity.ok(obtenerMetricasUseCase.getProcessHealth());
    }

    @GetMapping("/ai-metrics")
    public ResponseEntity<AiMetricsDTO> getAiMetrics() {
        return ResponseEntity.ok(obtenerMetricasUseCase.getAiMetrics());
    }
}
