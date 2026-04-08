package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.FormCertificationService;
import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Endpoint para gestión inmutable de versiones JSONB de Formularios (US-003, CA-87).
 * Integrado con FormCertificationService para CA-12 (revocación automática al mutar esquema).
 */
@RestController
@RequestMapping("/api/v1/design/form-definitions")
@Tag(name = "Form Definitions", description = "Endpoints para la gestión de versiones inmutables del esquema JSONB de formularios")
public class FormDefinitionController {

    private final FormDefinitionRepository formDefinitionRepository;
    private final FormCertificationService certificationService;

    public FormDefinitionController(FormDefinitionRepository formDefinitionRepository,
                                     FormCertificationService certificationService) {
        this.formDefinitionRepository = formDefinitionRepository;
        this.certificationService = certificationService;
    }

    @Operation(summary = "Crear/Actualizar versión de diseño", description = "Persiste el AST del formulario en formato JSONB inmutable. Aplica CA-12: revocación de sello QA si el esquema muta.")
    @PostMapping("/{formId}")
    public ResponseEntity<?> saveFormVersion(@PathVariable UUID formId, @RequestBody String schemaContent) {
        Optional<FormDefinitionEntity> existing = formDefinitionRepository.findById(formId);
        FormDefinitionEntity entity;

        if (existing.isPresent()) {
            entity = existing.get();
            entity.setSchemaContent(schemaContent);
            entity.setHashSha256(computeSha256(schemaContent));
            formDefinitionRepository.save(entity);
            // CA-12: Trigger revocation check
            certificationService.onSchemaModified(formId, "system");
            // Re-read after revocation
            entity = formDefinitionRepository.findById(formId).orElse(entity);
        } else {
            entity = new FormDefinitionEntity();
            entity.setId(formId);
            entity.setFormId(formId);
            entity.setVersionId(1);
            entity.setSchemaContent(schemaContent);
            entity.setCreatedBy("system");
            entity.setHashSha256(computeSha256(schemaContent));
            entity.setIsQaCertified(false);
            formDefinitionRepository.save(entity);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", entity.getId());
        response.put("form_id", entity.getFormId());
        response.put("is_qa_certified", entity.getIsQaCertified());
        response.put("hash_sha256", entity.getHashSha256());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar versiones de formulario", description = "Retorna el historial de diseño para permitir Rollbacks (Audit).")
    @GetMapping("/{formId}/versions")
    public ResponseEntity<?> getFormVersions(@PathVariable UUID formId) {
        return ResponseEntity.ok(formDefinitionRepository.findByFormIdOrderByVersionIdDesc(formId));
    }

    private String computeSha256(String content) {
        if (content == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
