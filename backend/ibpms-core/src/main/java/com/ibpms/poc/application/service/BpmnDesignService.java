package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.BpmnProcessDesignDTO;
import com.ibpms.poc.application.dto.CreateBpmnProcessDesignDTO;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio principal del BPMN Designer.
 * Orquesta: CRUD, Auto-Save (CA-10), Lock Pesimista (CA-7/CA-34), Auditoría
 * (CA-33).
 */
@Service
@Transactional
public class BpmnDesignService {

    private final BpmnProcessDesignRepository designRepository;
    private final BpmnDesignAuditLogRepository auditRepository;

    public BpmnDesignService(BpmnProcessDesignRepository designRepository,
            BpmnDesignAuditLogRepository auditRepository) {
        this.designRepository = designRepository;
        this.auditRepository = auditRepository;
    }

    // --- CRUD ---

    public BpmnProcessDesignDTO crear(CreateBpmnProcessDesignDTO dto, String createdBy) {
        BpmnProcessDesign domain = BpmnProcessDesign.crear(
                dto.getName(),
                BpmnProcessDesign.FormPattern.valueOf(dto.getFormPattern()),
                createdBy);

        BpmnProcessDesignEntity entity = toEntity(domain);
        designRepository.save(entity);

        audit(domain.getId(), BpmnDesignAuditLogEntity.Action.EDIT, createdBy,
                0, "{\"event\":\"CREATED\"}");

        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public BpmnProcessDesignDTO obtener(UUID id) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        autoReleaseLockIfExpired(entity);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<BpmnProcessDesignDTO> listar() {
        return designRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void archivar(UUID id, String userId) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        // CA-23: Bloqueado si hay instancias en vuelo (delegado al Controller que
        // valida con Camunda)
        entity.setStatus(BpmnProcessDesignEntity.Status.ARCHIVED);
        entity.setUpdatedAt(LocalDateTime.now());
        designRepository.save(entity);

        audit(id, BpmnDesignAuditLogEntity.Action.ARCHIVE, userId,
                entity.getCurrentVersion(), null);
    }

    // --- Auto-Save (CA-10) ---

    public void guardarBorrador(UUID id, String xml, String userId) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        entity.setXmlDraft(xml);
        entity.setUpdatedAt(LocalDateTime.now());
        designRepository.save(entity);

        audit(id, BpmnDesignAuditLogEntity.Action.SAVE_DRAFT, userId,
                entity.getCurrentVersion(), null);
    }

    // --- Lock Pesimista (CA-7 / CA-34) ---

    public void acquireLock(UUID id, String userId) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        autoReleaseLockIfExpired(entity);

        if (entity.getLockedBy() != null && !entity.getLockedBy().equals(userId)) {
            throw new com.ibpms.poc.domain.exception.ProcessDesignLockedException(
                    "El proceso está bloqueado por " + entity.getLockedBy());
        }
        entity.setLockedBy(userId);
        entity.setLockedAt(LocalDateTime.now());
        designRepository.save(entity);

        audit(id, BpmnDesignAuditLogEntity.Action.LOCK, userId,
                entity.getCurrentVersion(), null);
    }

    public void releaseLock(UUID id, String userId) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        if (entity.getLockedBy() != null && !entity.getLockedBy().equals(userId)) {
            throw new com.ibpms.poc.domain.exception.ProcessDesignLockedException(
                    "Solo " + entity.getLockedBy() + " puede liberar el bloqueo.");
        }
        entity.setLockedBy(null);
        entity.setLockedAt(null);
        designRepository.save(entity);

        audit(id, BpmnDesignAuditLogEntity.Action.UNLOCK, userId,
                entity.getCurrentVersion(), null);
    }

    // --- Request Deploy (CA-25) ---

    public void requestDeploy(UUID id, String userId) {
        BpmnProcessDesignEntity entity = findOrFail(id);
        if (entity.getStatus() != BpmnProcessDesignEntity.Status.DRAFT
                && entity.getStatus() != BpmnProcessDesignEntity.Status.ACTIVE) {
            throw new IllegalStateException("Solo procesos en DRAFT o ACTIVE pueden solicitar despliegue.");
        }
        entity.setStatus(BpmnProcessDesignEntity.Status.PENDING_DEPLOY);
        entity.setUpdatedAt(LocalDateTime.now());
        designRepository.save(entity);

        audit(id, BpmnDesignAuditLogEntity.Action.REQUEST_DEPLOY, userId,
                entity.getCurrentVersion(), null);
    }

    // --- Helpers ---

    public BpmnProcessDesignEntity findOrFail(UUID id) {
        return designRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Diseño BPMN no encontrado: " + id));
    }

    private void autoReleaseLockIfExpired(BpmnProcessDesignEntity entity) {
        if (entity.getLockedAt() != null
                && entity.getLockedAt().plusMinutes(30).isBefore(LocalDateTime.now())) {
            entity.setLockedBy(null);
            entity.setLockedAt(null);
            designRepository.save(entity);
        }
    }

    private void audit(UUID designId, BpmnDesignAuditLogEntity.Action action,
            String userId, int version, String details) {
        BpmnDesignAuditLogEntity audit = new BpmnDesignAuditLogEntity(
                designId, action, userId, version, details);
        auditRepository.save(audit);
    }

    // --- Mappers manuales ---

    private BpmnProcessDesignEntity toEntity(BpmnProcessDesign domain) {
        BpmnProcessDesignEntity e = new BpmnProcessDesignEntity();
        e.setId(domain.getId());
        e.setName(domain.getName());
        e.setTechnicalId(domain.getTechnicalId());
        e.setFormPattern(BpmnProcessDesignEntity.FormPattern.valueOf(domain.getFormPattern().name()));
        e.setStatus(BpmnProcessDesignEntity.Status.valueOf(domain.getStatus().name()));
        e.setCurrentVersion(domain.getCurrentVersion());
        e.setLockedBy(domain.getLockedBy());
        e.setLockedAt(domain.getLockedAt());
        e.setXmlDraft(domain.getXmlDraft());
        e.setMaxNodes(domain.getMaxNodes());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        e.setCreatedBy(domain.getCreatedBy());
        return e;
    }

    private BpmnProcessDesignDTO toDto(BpmnProcessDesignEntity e) {
        BpmnProcessDesignDTO dto = new BpmnProcessDesignDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setTechnicalId(e.getTechnicalId());
        dto.setFormPattern(e.getFormPattern().name());
        dto.setStatus(e.getStatus().name());
        dto.setCurrentVersion(e.getCurrentVersion());
        dto.setLockedBy(e.getLockedBy());
        dto.setLockedAt(e.getLockedAt());
        dto.setXmlDraft(e.getXmlDraft());
        dto.setMaxNodes(e.getMaxNodes());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        return dto;
    }
}
