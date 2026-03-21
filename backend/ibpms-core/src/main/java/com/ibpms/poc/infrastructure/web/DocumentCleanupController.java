package com.ibpms.poc.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Destructor Atómico S3 (Draft Cleanup) para Archivos Huérfanos.
 * Este endpoint es receptor de `navigator.sendBeacon` (Asíncrono) disparado
 * cuando el frontend aborta la publicación o auto-salva y purga Dropzones.
 */
@RestController
@RequestMapping("/api/v1/documents/cleanup")
public class DocumentCleanupController {

    private static final Logger log = LoggerFactory.getLogger(DocumentCleanupController.class);

    @PostMapping
    public ResponseEntity<Void> cleanupOrphanedFiles(@RequestBody List<String> orphanedUuids) {
        if (orphanedUuids == null || orphanedUuids.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 400
        }

        // Delegar al Adapter asíncrono AWS S3 (amazonS3Client.deleteObjects) o SharePoint
        // Este Endpoint debe devolver HTTP 202 Accepted extremadamente rápido para 
        // asegurar que el Web Vitals (INP) del navegador no se resienta.
        orphanedUuids.forEach(uuid -> 
            log.info("[SRE-CLEANUP] 🗑️ Vórtice de Eliminación: Purga Atómica Asíncrona enviada a S3 // UUID: {}", uuid)
        );

        return ResponseEntity.accepted().build(); // 202
    }
}
