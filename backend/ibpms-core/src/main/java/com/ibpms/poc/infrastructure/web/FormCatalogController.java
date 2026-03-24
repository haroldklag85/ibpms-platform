package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller Mock del Catálogo de Formularios.
 * Puente entre el Motor BPMN de Diseño y el Constructor de Formularios (CA-39).
 */
@RestController
@RequestMapping("/api/v1/forms")
public class FormCatalogController {

    /**
     * Root GET movido a FormDirectoryController para evitar colisiones.
     */
    /*
    @GetMapping
    public ResponseEntity<List<Object>> rootPath() {
        return ResponseEntity.ok(Collections.emptyList());
    }
    */

    /**
     * CA-39: Directorio transversal de formularios activos.
     * Nutre el dropdown de "Form Key" en el BPMN Modeler.
     * (Mapeo desplazado a /active para evitar AmbiguousHandler con FormDirectoryController de la UI principal).
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveForms() {
        return ResponseEntity.ok(List.of(
            Map.of("id", "frm_aprobacion", "name", "Formulario Aprobación", "type", "SIMPLE"),
            Map.of("id", "frm_onboarding_master", "name", "Onboarding Integral", "type", "MASTER", "stages", 4),
            Map.of("id", "frm_reembolso_gastos", "name", "Reembolso de Gastos", "type", "SIMPLE")
        ));
    }
}
