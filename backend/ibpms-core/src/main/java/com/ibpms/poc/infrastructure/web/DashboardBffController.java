package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoint BFF para renderizado Server-Driven UI y Cards Dinámicas basadas en Rol.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardBffController {

    /**
     * US-025 CA-11: Retorna Cards visibles para el rol específico (Filtrado dinámico).
     * Nota: En esta iteración simulamos métricas clave de Workbox.
     */
    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getDashboardCards(@RequestParam(required = false) UUID role) {
        // Retornamos un Payload Dinámico (Server-Driven UI Pattern)
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "card-1",
                        "title", "Tareas Disponibles",
                        "value", "14",
                        "icon", "inbox",
                        "route", "/agile/workbox"
                ),
                Map.of(
                        "id", "card-2",
                        "title", "Triage Pendiente",
                        "value", "3",
                        "icon", "alert-circle",
                        "route", "/agile/triage" // Solo visible si el UI formatea correctamente, mock por ahora
                )
        ));
    }
}
