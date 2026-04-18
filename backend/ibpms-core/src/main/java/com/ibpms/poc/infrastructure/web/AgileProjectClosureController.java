package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileProjectClosureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agile/projects")
public class AgileProjectClosureController {

    private final AgileProjectClosureService closureService;

    public AgileProjectClosureController(AgileProjectClosureService closureService) {
        this.closureService = closureService;
    }

    // CA-10: Cierre en Cascada
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeProjectInCascade(@PathVariable UUID id) {
        String closedBy = "admin"; // Simulado
        closureService.closeProjectInCascade(id, closedBy);
        return ResponseEntity.noContent().build();
    }
}
