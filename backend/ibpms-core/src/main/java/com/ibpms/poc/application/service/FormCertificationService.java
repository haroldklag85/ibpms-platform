package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.FormDefinitionEntity;
import com.ibpms.poc.infrastructure.jpa.repository.FormDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private final FormDefinitionRepository formDefinitionRepository;
    private final JdbcTemplate jdbcTemplate;

    public FormCertificationService(FormDefinitionRepository formDefinitionRepository,
                                     JdbcTemplate jdbcTemplate) {
        this.formDefinitionRepository = formDefinitionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ─────────────────────────────────────────────
    // Ensure entity exists (create stub if missing)
    // ─────────────────────────────────────────────

    @Transactional
    public void ensureEntityExists(UUID formDefinitionId) {
        if (!formDefinitionRepository.existsById(formDefinitionId)) {
            FormDefinitionEntity stub = new FormDefinitionEntity();
            stub.setId(formDefinitionId);
            stub.setFormId(formDefinitionId);
            stub.setVersionId(1);
            stub.setSchemaContent("{}");
            stub.setCreatedBy("system");
            stub.setHashSha256(computeSha256("{}"));
            stub.setIsQaCertified(false);
            formDefinitionRepository.save(stub);
            log.info("Auto-created stub FormDefinitionEntity for {}", formDefinitionId);
        }
    }

    // ─────────────────────────────────────────────
    // CA-16: Certificar con concurrencia optimista
    // ─────────────────────────────────────────────

    @Transactional
    public FormDefinitionEntity certifyForm(UUID formDefinitionId, String certifierUserId, String payloadJson) {
        FormDefinitionEntity entity = formDefinitionRepository.findById(formDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FormDefinition not found: " + formDefinitionId));

        // CA-16: Concurrencia optimista — si ya está certificada, rechazar
        if (Boolean.TRUE.equals(entity.getIsQaCertified()) && entity.getCertifiedBy() != null) {
            long secondsAgo = ChronoUnit.SECONDS.between(entity.getCertifiedAt(), LocalDateTime.now());
            String message = "Este esquema ya fue certificado por " + entity.getCertifiedBy() +
                    " hace " + secondsAgo + " segundos. Recargue para ver el estado actualizado.";

            // Registrar intento rechazado en audit log
            auditLog(certifierUserId, "QA_CERT_CONFLICT",
                    "{\"reason\": \"Concurrent certification attempt rejected\", \"existingCertifier\": \"" + entity.getCertifiedBy() + "\"}",
                    null);

            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }

        // Certificar
        String currentHash = computeSha256(entity.getSchemaContent());
        entity.setIsQaCertified(true);
        entity.setCertifiedSchemaHash(currentHash);
        entity.setCertifiedBy(certifierUserId);
        entity.setCertifiedAt(LocalDateTime.now());
        formDefinitionRepository.save(entity);

        // CA-15: Registrar en audit log con payload truncamiento
        auditLog(certifierUserId, "QA_CERTIFIED",
                "{\"formDefinitionId\": \"" + formDefinitionId + "\", \"hash\": \"" + currentHash + "\"}",
                payloadJson);

        log.info("QA Certification granted for FormDefinition {} by {}", formDefinitionId, certifierUserId);
        return entity;
    }

    // ──────────────────────────────────────────────────────
    // CA-12: Revocación automática por mutación del esquema
    // ──────────────────────────────────────────────────────

    @Transactional
    public void onSchemaModified(UUID formDefinitionId, String modifiedBy) {
        FormDefinitionEntity entity = formDefinitionRepository.findById(formDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FormDefinition not found"));

        if (!Boolean.TRUE.equals(entity.getIsQaCertified())) {
            return; // Not certified, nothing to revoke
        }

        String previousHash = entity.getCertifiedSchemaHash();
        String newHash = computeSha256(entity.getSchemaContent());

        if (previousHash != null && !previousHash.equals(newHash)) {
            // Revocar sello
            entity.setIsQaCertified(false);
            entity.setCertifiedSchemaHash(null);
            entity.setCertifiedBy(null);
            entity.setCertifiedAt(null);
            formDefinitionRepository.save(entity);

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
    public FormDefinitionEntity createNewVersion(UUID formId, int newVersionId, String schemaContent, String createdBy) {
        FormDefinitionEntity newVersion = new FormDefinitionEntity();
        newVersion.setFormId(formId);
        newVersion.setVersionId(newVersionId);
        newVersion.setSchemaContent(schemaContent);
        newVersion.setCreatedBy(createdBy);
        newVersion.setHashSha256(computeSha256(schemaContent));

        // CA-13: El sello NO se hereda. Nace sin certificar.
        newVersion.setIsQaCertified(false);
        newVersion.setCertifiedSchemaHash(null);
        newVersion.setCertifiedBy(null);
        newVersion.setCertifiedAt(null);

        formDefinitionRepository.save(newVersion);
        log.info("New schema version V{} created for form {} — born uncertified (CA-13)", newVersionId, formId);
        return newVersion;
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

        jdbcTemplate.update(
                "INSERT INTO ibpms_audit_log (id, entity_type, entity_id, event_type, performed_by, created_at, payload_snapshot, is_compressed, truncated, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)",
                id, "FORM_DEFINITION", id, action, userId,
                java.sql.Timestamp.valueOf(LocalDateTime.now()),
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
}
