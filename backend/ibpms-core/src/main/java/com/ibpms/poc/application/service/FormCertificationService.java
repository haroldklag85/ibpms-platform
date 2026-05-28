package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.FormCertification;
import com.ibpms.poc.domain.model.FormDefinition;
import com.ibpms.poc.application.port.out.FormDefinitionPort;
import com.ibpms.poc.application.port.out.FormCertificationPort;
import com.ibpms.poc.application.port.out.AuditLogPort;
import com.ibpms.poc.infrastructure.web.dto.FormDefinitionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * CA-12: Revocación automática del sello QA por mutación del esquema.
 * CA-13: Versionado del sello por generación del esquema (sin herencia).
 * CA-15: Truncamiento y compresión GZIP del payload en audit log.
 * CA-16: Control de concurrencia optimista en certificación simultánea.
 */
@Service
public class FormCertificationService {

    private static final Logger log = LoggerFactory.getLogger(FormCertificationService.class);
    private static final int PAYLOAD_RAW_LIMIT = 32 * 1024;       // 32KB
    private static final int PAYLOAD_COMPRESSED_LIMIT = 64 * 1024; // 64KB

    private final FormDefinitionPort formDefinitionPort;
    private final FormCertificationPort formCertificationPort;
    private final AuditLogPort auditLogPort;
    private final ObjectMapper objectMapper;

    public FormCertificationService(FormDefinitionPort formDefinitionPort,
                                     FormCertificationPort formCertificationPort,
                                     AuditLogPort auditLogPort,
                                     ObjectMapper objectMapper) {
        this.formDefinitionPort = formDefinitionPort;
        this.formCertificationPort = formCertificationPort;
        this.auditLogPort = auditLogPort;
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────
    // Ensure entity exists (create stub if missing)
    // ─────────────────────────────────────────────

    @Transactional
    public void ensureEntityExists(UUID formDefinitionId) {
        if (!formDefinitionPort.existsById(formDefinitionId)) {
            FormDefinition stub = new FormDefinition();
            stub.setId(formDefinitionId);
            stub.setFormId(formDefinitionId);
            stub.setVersionId(1);
            stub.setSchemaContent("{}");
            stub.setCreatedBy("system");
            stub.setHashSha256(computeSha256("{}"));
            formDefinitionPort.save(stub);

            FormCertification cert = new FormCertification();
            cert.setFormDefinitionId(formDefinitionId);
            cert.setIsQaCertified(false);
            formCertificationPort.save(cert);

            log.info("Auto-created stub FormDefinition for {}", formDefinitionId);
        }
    }

    // ─────────────────────────────────────────────
    // CA-16: Certificar con concurrencia optimista
    // ─────────────────────────────────────────────

    @Transactional
    public FormDefinitionDTO certifyForm(UUID formDefinitionId, String certifierUserId, String payloadJson) {
        FormDefinition entity = formDefinitionPort.findById(formDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FormDefinition not found: " + formDefinitionId));

        FormCertification cert = formCertificationPort.findByFormDefinitionId(formDefinitionId)
                .orElseGet(() -> {
                    FormCertification newCert = new FormCertification();
                    newCert.setFormDefinitionId(formDefinitionId);
                    return newCert;
                });

        // CA-16: Concurrencia optimista — si ya está certificada, rechazar
        if (Boolean.TRUE.equals(cert.getIsQaCertified()) && cert.getCertifiedBy() != null) {
            long secondsAgo = ChronoUnit.SECONDS.between(cert.getCertifiedAt(), LocalDateTime.now());
            String message = "Este esquema ya fue certificado por " + cert.getCertifiedBy() +
                    " hace " + secondsAgo + " segundos. Recargue para ver el estado actualizado.";

            // Registrar intento rechazado en audit log
            auditLog(certifierUserId, "QA_CERT_CONFLICT",
                    "{\"reason\": \"Concurrent certification attempt rejected\", \"existingCertifier\": \"" + cert.getCertifiedBy() + "\"}",
                    null);

            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }

        // Certificar
        String currentHash = computeSha256(entity.getSchemaContent());
        cert.setIsQaCertified(true);
        cert.setCertifiedSchemaHash(currentHash);
        cert.setCertifiedBy(certifierUserId);
        cert.setCertifiedAt(LocalDateTime.now());
        formCertificationPort.save(cert);

        // CA-15: Registrar en audit log con payload truncamiento
        auditLog(certifierUserId, "QA_CERTIFIED",
                "{\"formDefinitionId\": \"" + formDefinitionId + "\", \"hash\": \"" + currentHash + "\"}",
                payloadJson);

        log.info("QA Certification granted for FormDefinition {} by {}", formDefinitionId, certifierUserId);
        
        return toDTO(entity, cert);
    }

    // ──────────────────────────────────────────────────────
    // CA-12: Revocación automática por mutación del esquema
    // ──────────────────────────────────────────────────────

    @Transactional
    public void onSchemaModified(UUID formDefinitionId, String modifiedBy) {
        FormDefinition entity = formDefinitionPort.findById(formDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FormDefinition not found"));

        Optional<FormCertification> certOpt = formCertificationPort.findByFormDefinitionId(formDefinitionId);
        if (certOpt.isEmpty() || !Boolean.TRUE.equals(certOpt.get().getIsQaCertified())) {
            return; // Not certified, nothing to revoke
        }

        FormCertification cert = certOpt.get();
        String previousHash = cert.getCertifiedSchemaHash();
        String newHash = computeSha256(entity.getSchemaContent());

        if (previousHash != null && !previousHash.equals(newHash)) {
            // Revocar sello
            cert.setIsQaCertified(false);
            cert.setCertifiedSchemaHash(null);
            cert.setCertifiedBy(null);
            cert.setCertifiedAt(null);
            formCertificationPort.save(cert);

            // Audit log inmutable
            String details = "{\"action\": \"QA_CERT_REVOKED\", \"reason\": \"Schema modified post-certification\"," +
                    " \"previousHash\": \"" + previousHash + "\", \"newHash\": \"" + newHash + "\"," +
                    " \"modifiedBy\": \"" + modifiedBy + "\"}";
            auditLog(modifiedBy, "QA_CERT_REVOKED", details, null);

            log.warn("QA Certification REVOKED for FormDefinition {} — schema modified by {}", formDefinitionId, modifiedBy);
        }
    }

    // ────────────────────────────────────────────────────────
    // CA-13: Nueva versión nace sin sello (sin herencia)
    // ────────────────────────────────────────────────────────

    @Transactional
    public FormDefinitionDTO createNewVersion(UUID formId, int newVersionId, String schemaContent, String createdBy) {
        try {
            objectMapper.readTree(schemaContent);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Form schemaContent is not a valid JSON string");
        }

        FormDefinition newVersion = new FormDefinition();
        newVersion.setFormId(formId);
        newVersion.setVersionId(newVersionId);
        newVersion.setSchemaContent(schemaContent);
        newVersion.setCreatedBy(createdBy);
        newVersion.setHashSha256(computeSha256(schemaContent));

        // [LEY GLOBAL 3: Trazabilidad Inversa] - US-003 - CA-87: Asignar la entidad guardada con ID generado para evitar form_definition_id null
        FormDefinition savedVersion = formDefinitionPort.save(newVersion);

        // CA-13: El sello NO se hereda. Nace sin certificar.
        FormCertification newCert = new FormCertification();
        newCert.setFormDefinitionId(savedVersion.getId());
        newCert.setIsQaCertified(false);
        newCert.setCertifiedSchemaHash(null);
        newCert.setCertifiedBy(null);
        newCert.setCertifiedAt(null);
        
        formCertificationPort.save(newCert);

        log.info("New schema version V{} created for form {} — born uncertified (CA-13)", newVersionId, formId);
        return toDTO(savedVersion, newCert);
    }

    // ───────────────────────────────────────────────────────────
    // CA-15: Audit log con truncamiento/compresión GZIP
    // ───────────────────────────────────────────────────────────

    private void auditLog(String userId, String action, String detailsJson, String payloadJson) {
        String id = UUID.randomUUID().toString();
        byte[] payloadBytes = null;
        boolean isCompressed = false;
        boolean truncated = false;

        if (payloadJson != null && !payloadJson.isBlank()) {
            byte[] rawBytes = payloadJson.getBytes(StandardCharsets.UTF_8);

            if (rawBytes.length <= PAYLOAD_RAW_LIMIT) {
                // < 32KB → store raw
                payloadBytes = rawBytes;
            } else {
                // >= 32KB → compress with GZIP
                byte[] compressed = gzipCompress(rawBytes);
                if (compressed.length <= PAYLOAD_COMPRESSED_LIMIT) {
                    payloadBytes = compressed;
                    isCompressed = true;
                } else {
                    // Still > 64KB after compression → truncate raw to 32KB
                    payloadBytes = new byte[PAYLOAD_RAW_LIMIT];
                    System.arraycopy(rawBytes, 0, payloadBytes, 0, PAYLOAD_RAW_LIMIT);
                    truncated = true;
                }
            }
        }

        auditLogPort.saveAuditLog(
                id, "FORM_DEFINITION", id, action, userId,
                LocalDateTime.now(),
                payloadBytes, isCompressed, truncated, detailsJson
        );
    }

    // ─────────────────────── Utilities ───────────────────────

    private String computeSha256(String content) {
        if (content == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private byte[] gzipCompress(byte[] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
            gzip.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("GZIP compression failed", e);
            return data; // Fallback: return raw
        }
    }
    
    private FormDefinitionDTO toDTO(FormDefinition def, FormCertification cert) {
        FormDefinitionDTO dto = new FormDefinitionDTO();
        dto.setId(def.getId());
        dto.setFormId(def.getFormId());
        dto.setVersionId(def.getVersionId());
        dto.setSchemaContent(def.getSchemaContent());
        dto.setCreatedBy(def.getCreatedBy());
        dto.setCreatedAt(def.getCreatedAt());
        dto.setHashSha256(def.getHashSha256());

        if (cert != null) {
            dto.setIsQaCertified(cert.getIsQaCertified());
            dto.setCertifiedSchemaHash(cert.getCertifiedSchemaHash());
            dto.setCertifiedBy(cert.getCertifiedBy());
            dto.setCertifiedAt(cert.getCertifiedAt());
        } else {
            dto.setIsQaCertified(false);
        }
        return dto;
    }
}
