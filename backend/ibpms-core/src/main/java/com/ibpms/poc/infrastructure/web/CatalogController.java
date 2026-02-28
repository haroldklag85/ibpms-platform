package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Adaptador Driving — Controlador REST de Catálogos (Reference Data).
 * Endpoint requerido por la UI dinámica (Server-Driven UI) para llenar combos.
 * En V1 PoC responde datos estáticos en memoria (dummy data).
 */
import com.ibpms.poc.application.port.out.CrmFederationPort;

@RestController
@RequestMapping("/api/v1/catalogs")
public class CatalogController {

    private final CrmFederationPort crmPort;

    public CatalogController(CrmFederationPort crmPort) {
        this.crmPort = crmPort;
    }

    @GetMapping(value = "/{catalogId}", produces = "application/json")
    public ResponseEntity<String> getCatalog(@PathVariable String catalogId) {
        // Enrutado abstracto: El Facade delega al Adaptador.
        // Si el CRM cae, Resonance4J abrirá el circuito y devolverá de la BD local
        String catalogData = crmPort.fetchCatalogFromCrm(catalogId);
        return ResponseEntity.ok(catalogData);
    }
}
