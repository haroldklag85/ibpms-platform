package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.integration.ApiConnectorService;
import com.ibpms.poc.infrastructure.jpa.entity.integration.ApiConnectorEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration/connectors")
@CrossOrigin(origins = "*") // Para desarrollo Frontend Vite
public class ApiConnectorController {

    private final ApiConnectorService service;

    public ApiConnectorController(ApiConnectorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ApiConnectorEntity>> listAll() {
        return ResponseEntity.ok(service.listAllConnectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiConnectorEntity> getById(@PathVariable UUID id) {
        return service.getConnector(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiConnectorEntity> create(@RequestBody ApiConnectorEntity connector) {
        return ResponseEntity.ok(service.createConnector(connector));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiConnectorEntity> update(@PathVariable UUID id, @RequestBody ApiConnectorEntity connector) {
        try {
            return ResponseEntity.ok(service.updateConnector(id, connector));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteConnector(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint invocado desde Frontend Testing Playground.
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testPing(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        try {
            // El Frontend nos envía { "payload": "{...}" }
            String payloadJson = (String) body.getOrDefault("payload", "{}");
            String rawResponse = service.testPing(id, payloadJson);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "code", 200,
                    "data", rawResponse));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "code", 500,
                    "message", e.getMessage()));
        }
    }
}
