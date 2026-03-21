package com.ibpms.poc.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CA-77: Hub Homologado de Integraciones (Anti-SSRF).
 * Recibe peticiones de Autocompletado del Frontend hacia servicios externos.
 * El Backend NO es un Proxy ciego. Valida contra un catálogo autorizado.
 */
@RestController
@RequestMapping("/api/v1/integrations/execute")
public class ApiConnectorController {

    private static final Logger log = LoggerFactory.getLogger(ApiConnectorController.class);

    // Whitelist Estricto de Conectores (En V2 vivirían en BD o Consul)
    private static final List<String> AUTHORIZED_CONNECTORS = List.of(
        "RENIEC_PROXY",
        "CRM_SALESFORCE",
        "SAP_ERP_CLIENTS"
    );

    @PostMapping("/{connectorId}")
    public ResponseEntity<Map<String, Object>> executeConnector(
            @PathVariable String connectorId,
            @RequestBody Map<String, Object> integrationPayload) {

        if (!AUTHORIZED_CONNECTORS.contains(connectorId)) {
            log.warn("[SRE-SEC] ⛔ Intento de SSRF detectado por el frontend. ConnectorID falso: {}", connectorId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "SSRF Blocked: Connector [" + connectorId + "] is strictly prohibited."));
        }

        log.info("[B2B-HUB] 🔌 Ejecutando passthrough homologado hacia Malla Externa: {}", connectorId);

        // Mock de Respuesta hacia APIs externas
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "mockData", "Datos resueltos remotamente y sanitizados",
            "resolvedId", connectorId
        ));
    }
}
