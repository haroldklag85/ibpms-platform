package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileProjectService;
import com.ibpms.poc.domain.model.agile.AgileProject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agile/projects")
public class AgileProjectController {

    private final AgileProjectService projectService;

    public AgileProjectController(AgileProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<AgileProject> createProject(
            @Valid @RequestBody CreateAgileProjectRequest request,
            Authentication authentication) {
        String createdBy = authentication.getName();
        return ResponseEntity.ok(projectService.createProject(request.name(), request.description(), createdBy));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Page<AgileProject>> listProjects(Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<AgileProject> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    public record CreateAgileProjectRequest(
            @NotBlank(message = "El nombre es obligatorio") String name,
            String description
    ) {}
}
