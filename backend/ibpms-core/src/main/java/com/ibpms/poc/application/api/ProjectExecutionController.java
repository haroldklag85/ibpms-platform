package com.ibpms.poc.application.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/execution/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectExecutionController {


    @PostMapping("/{id}/baseline")
    @Transactional
    public ResponseEntity<String> setBaseline(@PathVariable String id) {
        log.info("Fijando Línea Base para el proyecto: {}", id);

        // Simulación: Invocar Camunda API local de forma transaccional (No REST HTTP)
        // runtimeService.startProcessInstanceByKey("project_execution_process", id);

        return ResponseEntity.ok("Línea Base fijada exitosamente.");
    }
}
