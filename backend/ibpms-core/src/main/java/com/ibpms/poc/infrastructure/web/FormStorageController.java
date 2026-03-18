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

    // CA-21: Almacenamiento SGDEA Intermedio
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
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
