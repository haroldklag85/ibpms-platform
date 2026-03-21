package com.ibpms.poc.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CA-74: Diccionario de Datos Global (B2B Hub).
 * Provee la lista oficial de variables corporativas para el Autocompletado del IDE,
 * previniendo el "Tower of Babel" de datos (Ej: cliente_id, cedula_cliente, idCliente).
 */
@RestController
@RequestMapping("/api/v1/design/dictionary")
public class FormDictionaryController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getGlobalDictionary() {
        // En V2 esto consumirá un Dominio MDM (Master Data Management). V1 Mock:
        return ResponseEntity.ok(List.of(
            Map.of(
                "id", "cliente_cedula",
                "label", "Cédula de Ciudadanía",
                "type", "number",
                "isPII", true,
                "domain", "Comercial",
                "zod", "z.string().min(5).max(12)"
            ),
            Map.of(
                "id", "monto_aprobado",
                "label", "Monto de Crédito (COP)",
                "type", "number",
                "isPII", false,
                "domain", "Riesgos",
                "zod", "z.number().positive()"
            )
        ));
    }
}
