package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileProjectClosureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import com.ibpms.poc.crosscutting.annotations.Traceability;

@RestController
@RequestMapping("/api/v1/agile/projects")
@Traceability(US = "US-030", CA = {"CA-10"})
public class AgileProjectClosureController {

    private final AgileProjectClosureService closureService;

    public AgileProjectClosureController(AgileProjectClosureService closureService) {
        this.closureService = closureService;
    }

    // CA-10: Cierre en Cascada
    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> closeProjectInCascade(@PathVariable UUID id) {
        String closedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        closureService.closeProjectInCascade(id, closedBy);
        return ResponseEntity.noContent().build();
    }
}
