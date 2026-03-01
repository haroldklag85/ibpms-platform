package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.ProjectTemplateDTO;
import com.ibpms.poc.application.port.in.CrearProjectTemplateUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project-templates")
public class ProjectTemplateController {

    private final CrearProjectTemplateUseCase crearProjectTemplateUseCase;

    public ProjectTemplateController(CrearProjectTemplateUseCase crearProjectTemplateUseCase) {
        this.crearProjectTemplateUseCase = crearProjectTemplateUseCase;
    }

    /**
     * Endpoint para crear una Plantilla WBS (Work Breakdown Structure).
     * Solo perfiles tipo "Arquitecto_BPM" o Administradores pueden acceder según la
     * US-006.
     */
    @PostMapping
    @PreAuthorize("hasRole('Architect') or hasRole('Admin_Intake')")
    public ResponseEntity<ProjectTemplateDTO> crearPlantilla(
            @RequestBody ProjectTemplateDTO dto,
            Authentication authentication) {

        String currentUser = authentication != null ? authentication.getName() : "system_admin";
        ProjectTemplateDTO result = crearProjectTemplateUseCase.crearPlantilla(dto, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
