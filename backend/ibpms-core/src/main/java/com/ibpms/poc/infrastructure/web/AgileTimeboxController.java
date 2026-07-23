package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTimeboxService;
import com.ibpms.poc.domain.model.agile.AgileTimebox;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de Timeboxes (Sprints) dentro de un Proyecto Ágil.
 * 
 * Iteración 2: Provee el contrato API cerrado para consumo por Frontend (Pinia Store).
 * El Agente Frontend debe mapear estos endpoints en su store sin ensamblar UI visual.
 */
@RestController
@RequestMapping("/api/v1/agile/timeboxes")
@Traceability(US = "US-030", CA = {"CA-01"})
public class AgileTimeboxController {

    private final AgileTimeboxService timeboxService;

    public AgileTimeboxController(AgileTimeboxService timeboxService) {
        this.timeboxService = timeboxService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<AgileTimebox> createTimebox(
            @Valid @RequestBody CreateTimeboxRequest request,
            Authentication authentication) {
        String createdBy = authentication.getName();
        AgileTimebox created = timeboxService.createTimebox(
                request.projectId(),
                request.name(),
                request.goal(),
                request.startDate(),
                request.endDate(),
                createdBy
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<AgileTimebox>> listTimeboxes(@RequestParam UUID projectId) {
        return ResponseEntity.ok(timeboxService.listTimeboxes(projectId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<AgileTimebox> getTimebox(@PathVariable UUID id) {
        return ResponseEntity.ok(timeboxService.getTimebox(id));
    }

    /**
     * DTO de request para la creación de un Timebox.
     * Contrato cerrado: Frontend consume este schema.
     */
    public record CreateTimeboxRequest(
            @NotNull(message = "El projectId es obligatorio") UUID projectId,
            @NotBlank(message = "El nombre es obligatorio") String name,
            String goal,
            @NotNull(message = "La fecha de inicio es obligatoria") LocalDate startDate,
            @NotNull(message = "La fecha de fin es obligatoria") LocalDate endDate
    ) {}
}
