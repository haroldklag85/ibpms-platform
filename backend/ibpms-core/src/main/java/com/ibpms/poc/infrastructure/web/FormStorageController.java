package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.FormStorageService;
import com.ibpms.poc.infrastructure.web.dto.FileUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms")
public class FormStorageController {

    private final FormStorageService formStorageService;

    public FormStorageController(FormStorageService formStorageService) {
        this.formStorageService = formStorageService;
    }

    // CA-39: Condicionamiento de Archivos Adjuntos - Seguridad
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        long maxSize = 10L * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "El archivo supera el límite de 10MB autorizado por CA-39."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("application/pdf") || 
                                     contentType.equals("image/jpeg") || 
                                     contentType.equals("image/png"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Tipo MIME no permitido. CA-39 solo aprueba PDF, JPEG y PNG."));
        }

        UUID documentId = UUID.randomUUID();
        String path = formStorageService.saveDocument(documentId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FileUploadResponse(documentId, path));
    }

    // CA-24: Auto-Guardado y Recuperación
    @PostMapping("/draft")
    public ResponseEntity<Void> saveDraft(@RequestBody Map<String, Object> payload) {
        UUID formId = null;
        if (payload.containsKey("id")) {
           try {
               formId = UUID.fromString(payload.get("id").toString());
           } catch (Exception e) {}
        }
        if (formId == null) { // Por si llega nulo o es the first auto-save de un file nuevo
            formId = UUID.randomUUID();
            payload.put("id", formId.toString());
        }
        formStorageService.execute(formId, payload);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/draft/{formId}")
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable("formId") UUID formId) {
        Map<String, Object> draft = formStorageService.getDraft(formId);
        if (draft == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(draft);
    }
}
