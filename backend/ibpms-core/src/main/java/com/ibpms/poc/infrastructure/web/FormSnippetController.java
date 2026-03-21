package com.ibpms.poc.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CA-74: Repositorio de Fragmentos de Formularios (Snippets).
 * Permite a los Arquitectos guardar y re-inyectar bloques enteros "Legos"
 * (Ej: Sección entera de 'Co-Deudores').
 */
@RestController
@RequestMapping("/api/v1/design/snippets")
public class FormSnippetController {

    // En V2 requerirá Base de Datos Relacional `ibpms_form_snippets`
    private final List<Map<String, Object>> inMemorySnippets = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listSnippets() {
        return ResponseEntity.ok(inMemorySnippets);
    }

    @PostMapping
    public ResponseEntity<Void> saveSnippet(@RequestBody Map<String, Object> snippetPayload) {
        if (!snippetPayload.containsKey("name") || !snippetPayload.containsKey("components")) {
            return ResponseEntity.badRequest().build();
        }
        
        inMemorySnippets.add(snippetPayload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
