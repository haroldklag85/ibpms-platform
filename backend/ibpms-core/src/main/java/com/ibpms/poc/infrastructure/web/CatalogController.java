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
@RestController
@RequestMapping("/catalogs")
public class CatalogController {

    @GetMapping("/{catalogId}")
    public ResponseEntity<List<Map<String, String>>> getCatalogItems(@PathVariable String catalogId) {

        switch (catalogId.toLowerCase()) {
            case "countries":
                return ResponseEntity.ok(List.of(
                        Map.of("code", "CO", "label", "Colombia"),
                        Map.of("code", "MX", "label", "México"),
                        Map.of("code", "AR", "label", "Argentina")));

            case "customer_types":
                return ResponseEntity.ok(List.of(
                        Map.of("code", "FISICA", "label", "Persona Física"),
                        Map.of("code", "JURIDICA", "label", "Persona Jurídica")));

            case "document_types":
                return ResponseEntity.ok(List.of(
                        Map.of("code", "CC", "label", "Cédula de Ciudadanía"),
                        Map.of("code", "CE", "label", "Cédula de Extranjería"),
                        Map.of("code", "PA", "label", "Pasaporte")));

            default:
                // Retornar vacío si no existe, como fallback amigable
                return ResponseEntity.ok(List.of());
        }
    }
}
