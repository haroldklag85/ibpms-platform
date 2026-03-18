package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.CreateFormDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.service.FormDesignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint del Motor de Formularios UI (Pantalla 7).
 * <p>
 * NOTA ARQUITECTÓNICA (US-028): Los Endpoints de Mapeo asíncrono y resolución de este Controlador 
 * DEBEN seguir siendo estructuralmente consumibles y mockeables por las pruebas BDD (Behavior-Driven Development) 
 * que auto-genera estáticamente el Frontend (Vitest/Zod). Las respuestas HTTP 200/201 deben mantener estricto tipping.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/forms")
public class FormDesignController {

    private final FormDesignService formDesignService;

    public FormDesignController(FormDesignService formDesignService) {
        this.formDesignService = formDesignService;
    }

    /**
     * Catálogo de formularios (solo última versión activa).
     */
    @GetMapping
    public ResponseEntity<List<FormDesignDTO>> listarCatalogo() {
        return ResponseEntity.ok(formDesignService.listarCatalogo());
    }

    /**
     * Obtener diseño histórico inmutable (Para renderizar Front en tareas viejas).
     */
    @GetMapping("/{technicalName}/versions/{version}")
    public ResponseEntity<FormDesignDTO> obtenerVersionInmutable(
            @PathVariable String technicalName,
            @PathVariable Integer version) {
        return ResponseEntity.ok(formDesignService.obtenerVersionInmutable(technicalName, version));
    }

    /**
     * CA-27: Clonador DB y Control de Versiones.
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<FormDesignDTO>> listarVersiones(@PathVariable UUID id) {
        return ResponseEntity.ok(formDesignService.listarVersiones(id));
    }

    /**
     * Crear nuevo formulario (Define patrón Dual).
     */
    @PostMapping
    public ResponseEntity<FormDesignDTO> crearFormulario(
            @Valid @RequestBody CreateFormDesignDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formDesignService.crear(dto, userId));
    }

    /**
     * Guardar/Actualizar. (Incrementa versión si ya está activo el V1).
     */
    @PostMapping("/{id}")
    public ResponseEntity<FormDesignDTO> guardarFormulario(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFormDesignDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.ok(formDesignService.actualizarOCrearVersion(id, dto, userId));
    }

    /**
     * Eliminar (Soft delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFormulario(@PathVariable UUID id) {
        formDesignService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * CA-26: Manejador de error semántico (Instancias activas).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<java.util.Map<String, String>> handleConflict(IllegalStateException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("bqueado (CA-26)") || ex.getMessage().contains("bloqueado")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", ex.getMessage()));
    }
}
