package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Hub de Integraciones Empresarial (Mock).
 * Despacha el catálogo de conectores OOTB para los ServiceTasks (CA-45).
 */
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationHubController {

    /**
     * CA-45: Catálogo Central de Conectores.
     */
    @GetMapping("/connectors")
    public ResponseEntity<List<Map<String, String>>> getConnectors() {
        return ResponseEntity.ok(List.of(
            Map.of("id", "o365_mail", "name", "Microsoft O365 / Exchange", "protocol", "REST"),
            Map.of("id", "sp_online", "name", "Microsoft SharePoint", "protocol", "REST"),
            Map.of("id", "oracle_ns", "name", "Oracle NetSuite", "protocol", "SOAP")
        ));
    }

    /**
     * CA-49: API de Esquemas del Hub.
     */
    @GetMapping("/connectors/{id}/schema")
    public ResponseEntity<List<Map<String, String>>> getConnectorSchema(@PathVariable("id") String id) {
        if ("o365_mail".equals(id)) {
            return ResponseEntity.ok(List.of(
                Map.of("field", "to", "type", "String"),
                Map.of("field", "body", "type", "String"),
                Map.of("field", "isHtml", "type", "Boolean")
            ));
        }
        return ResponseEntity.ok(List.of(
            Map.of("field", "endpoint", "type", "String"),
            Map.of("field", "payload", "type", "Object")
        ));
    }
}
