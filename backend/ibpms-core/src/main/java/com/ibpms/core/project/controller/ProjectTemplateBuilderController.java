package com.ibpms.core.project.controller;

import com.ibpms.core.project.dto.ProjectTemplateTreeDTO;
import com.ibpms.core.project.service.ProjectTemplateBuilderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/design/projects/templates")
public class ProjectTemplateBuilderController {

    private final ProjectTemplateBuilderService templateService;

    public ProjectTemplateBuilderController(ProjectTemplateBuilderService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<ProjectTemplateTreeDTO> deepSaveTemplate(@RequestBody ProjectTemplateTreeDTO payload) {
        try {
            ProjectTemplateTreeDTO savedTree = templateService.deepSaveTemplate(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTree);
        } catch (IllegalArgumentException e) {
            // Error Ciclo topologico u error general validacion
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<String> publishTemplate(@PathVariable String id) {
        try {
            templateService.publishTemplate(id);
            return ResponseEntity.ok("Template Published Successfully");
        } catch (IllegalStateException e) {
            // Error Integridad (AC-1) form_key == null
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
