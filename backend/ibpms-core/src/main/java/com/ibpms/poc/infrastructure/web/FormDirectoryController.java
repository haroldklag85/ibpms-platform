package com.ibpms.poc.infrastructure.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.application.service.form.FormDirectoryService;
import java.util.List;
import java.util.Map;

/**
 * Misión SRE-CA68: Nuevo Endpoint Controlador de Búsqueda de Formularios
 */
@RestController
@RequestMapping("/api/v1/forms")
public class FormDirectoryController {

    private final FormDirectoryService directoryService;

    @Autowired
    public FormDirectoryController(FormDirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> searchForms(
            @RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(directoryService.searchForms(search));
    }
}
