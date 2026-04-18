package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.BpmnProcessDesignDTO;
import com.ibpms.poc.application.dto.CreateBpmnProcessDesignDTO;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnProcessDesignRepository;
import com.ibpms.poc.infrastructure.jpa.entity.ProcessLockEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProcessLockRepository;
import com.ibpms.poc.infrastructure.jpa.entity.DeployRequestEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DeployRequestRepository;
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
    private final ProcessLockRepository processLockRepository;
    private final DeployRequestRepository deployRequestRepository;

    public BpmnDesignService(BpmnProcessDesignRepository designRepository,
            BpmnDesignAuditLogRepository auditRepository,
            ProcessLockRepository processLockRepository,
            DeployRequestRepository deployRequestRepository) {
        this.designRepository = designRepository;
        this.auditRepository = auditRepository;
        this.processLockRepository = processLockRepository;
        this.deployRequestRepository = deployRequestRepository;
    }

    // --- CRUD ---

    public BpmnProcessDesignDTO crear(CreateBpmnProcessDesignDTO dto, String createdBy) {
        BpmnProcessDesign domain = BpmnProcessDesign.crear(
                dto.getName(),
                BpmnProcessDesign.FormPattern.valueOf(dto.getFormPattern()),
                createdBy);

        BpmnProcessDesignEntity entity = toEntity(domain);
        designRepository.save(java.util.Objects.requireNonNull(entity));

        audit(domain.getId(), BpmnDesignAuditLogEntity.Action.EDIT, createdBy,
                0, "{\"event\":\"CREATED\"}");

        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public BpmnProcessDesignDTO obtener(UUID id) {
        BpmnProcessDesignEntity entity = findOrFail(id);
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

    // --- Configuración de Generic Form (CA-7) ---

    public void updateGenericFormConfig(String processKey, String whitelistJson, String userId) {
        BpmnProcessDesignEntity entity = designRepository.findByTechnicalId(processKey)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Diseño BPMN no encontrado con technical_id: " + processKey));
        
        entity.setGenericFormWhitelist(whitelistJson);
        entity.setUpdatedAt(LocalDateTime.now());
        designRepository.save(entity);

        audit(entity.getId(), BpmnDesignAuditLogEntity.Action.EDIT, userId,
                entity.getCurrentVersion(), "{\"event\":\"UPDATE_GENERIC_FORM_WHITELIST\"}");
    }

    // --- Lock Pesimista Separado (CA-66, CA-64) ---

    @Transactional
    public void acquireLockTechnicalKey(String processKey, String userId, String browserSessionId) {
        cleanStaleLock(processKey);
        processLockRepository.findById(processKey).ifPresent(lock -> {
            if (!lock.getLockedBy().equals(userId)) {
                throw new IllegalStateException("El proceso ya se encuentra bloqueado por otro usuario: " + lock.getLockedBy());
            }
        });
        processLockRepository.save(new ProcessLockEntity(processKey, userId, LocalDateTime.now(), browserSessionId));
        auditByTechnicalId(processKey, BpmnDesignAuditLogEntity.Action.LOCK, userId, "{\"session\": \"" + browserSessionId + "\"}");
    }

    @Transactional
    public void heartbeatLock(String processKey, String userId) {
        cleanStaleLock(processKey);
        ProcessLockEntity lock = processLockRepository.findById(processKey)
                .orElseThrow(() -> new IllegalStateException("No tienes un bloqueo activo sobre este proceso."));
        if (!lock.getLockedBy().equals(userId)) {
            throw new IllegalStateException("Este proceso está bloqueado por otro usuario.");
        }
        lock.setLockedAt(LocalDateTime.now());
        processLockRepository.save(lock);
    }

    @Transactional
    public void releaseLockTechnicalKey(String processKey, String userId) {
        processLockRepository.findById(processKey).ifPresent(lock -> {
            if (lock.getLockedBy().equals(userId)) {
                processLockRepository.delete(lock);
                auditByTechnicalId(processKey, BpmnDesignAuditLogEntity.Action.UNLOCK, userId, "{\"type\": \"normal\"}");
            }
        });
    }

    @Transactional
    public void forceReleaseLock(String processKey, String adminUserId) {
        processLockRepository.findById(processKey).ifPresent(lock -> {
            processLockRepository.delete(lock);
            auditByTechnicalId(processKey, BpmnDesignAuditLogEntity.Action.UNLOCK, adminUserId, "{\"type\": \"forced\", \"previousOwner\": \"" + lock.getLockedBy() + "\"}");
        });
    }

    private void cleanStaleLock(String processKey) {
        processLockRepository.findById(processKey).ifPresent(lock -> {
            if (lock.getLockedAt().isBefore(LocalDateTime.now().minusSeconds(90))) {
                processLockRepository.delete(lock);
                auditByTechnicalId(processKey, BpmnDesignAuditLogEntity.Action.UNLOCK, "SYSTEM", "{\"type\": \"stale_timeout\", \"previousOwner\": \"" + lock.getLockedBy() + "\"}");
            }
        });
    }

    private void auditByTechnicalId(String processKey, BpmnDesignAuditLogEntity.Action action, String userId, String details) {
        designRepository.findByTechnicalId(processKey).ifPresent(entity -> {
            audit(entity.getId(), action, userId, entity.getCurrentVersion(), details);
        });
    }

    // --- Request Deploy (CA-69) ---

    @Transactional
    public DeployRequestEntity createDeployRequest(String processKey, String requestedBy) {
        DeployRequestEntity req = new DeployRequestEntity(processKey, requestedBy);
        return deployRequestRepository.save(req);
    }

    @Transactional
    public DeployRequestEntity approveDeployRequest(UUID requestId, String reviewerId, String comment) {
        DeployRequestEntity req = deployRequestRepository.findById(requestId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Request not found"));
        req.setStatus(DeployRequestEntity.Status.APPROVED);
        req.setReviewedBy(reviewerId);
        req.setReviewedAt(LocalDateTime.now());
        req.setReviewComment(comment);
        return deployRequestRepository.save(req);
    }

    @Transactional
    public DeployRequestEntity rejectDeployRequest(UUID requestId, String reviewerId, String comment) {
        if (comment == null || comment.trim().length() < 20) {
            throw new IllegalArgumentException("El comentario de rechazo debe tener al menos 20 caracteres (CA-69).");
        }
        DeployRequestEntity req = deployRequestRepository.findById(requestId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Request not found"));
        req.setStatus(DeployRequestEntity.Status.REJECTED);
        req.setReviewedBy(reviewerId);
        req.setReviewedAt(LocalDateTime.now());
        req.setReviewComment(comment);
        return deployRequestRepository.save(req);
    }

    // --- Helpers ---

    public BpmnProcessDesignEntity findOrFail(UUID id) {
        return designRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Diseño BPMN no encontrado: " + id));
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
