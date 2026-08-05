// @Traceability: US-005, CA-42 - Activity Timeline
package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.BpmnProcessDesignDTO;
import com.ibpms.poc.application.dto.CreateBpmnProcessDesignDTO;
import com.ibpms.poc.application.dto.BpmnDesignAuditLogDTO;
import com.ibpms.poc.application.port.out.BpmnAuditPort;
import com.ibpms.poc.application.port.out.BpmnDesignPort;
import com.ibpms.poc.application.port.out.DeployRequestPort;
import com.ibpms.poc.application.port.out.ProcessLockPort;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Servicio principal del BPMN Designer.
 * Orquesta: CRUD, Auto-Save (CA-10), Lock Pesimista (CA-7/CA-34), Auditoría
 * (CA-33).
 * 
 * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
 */
@Service
@Traceability(US = "US-005", CA = {"CA-10", "CA-34"})
public class BpmnDesignService {

    private final BpmnDesignPort designPort;
    private final BpmnAuditPort auditPort;
    private final ProcessLockPort processLockPort;
    private final DeployRequestPort deployRequestPort;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public BpmnDesignService(BpmnDesignPort designPort,
                             BpmnAuditPort auditPort,
                             ProcessLockPort processLockPort,
                             DeployRequestPort deployRequestPort,
                             org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.designPort = designPort;
        this.auditPort = auditPort;
        this.processLockPort = processLockPort;
        this.deployRequestPort = deployRequestPort;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- CRUD ---

    public BpmnProcessDesignDTO crear(CreateBpmnProcessDesignDTO dto, String createdBy) {
        BpmnProcessDesign domain = BpmnProcessDesign.crear(
                dto.getName(),
                BpmnProcessDesign.FormPattern.valueOf(dto.getFormPattern()),
                createdBy);

        BpmnProcessDesign saved = designPort.save(domain);

        auditPort.logAction(saved.getId(), "EDIT", createdBy, 0, "{\"event\":\"CREATED\"}");

        return toDto(saved);
    }

    public BpmnProcessDesignDTO obtener(UUID id) {
        BpmnProcessDesign domain = getDomainModel(id);
        return toDto(domain);
    }

    public BpmnProcessDesignDTO obtenerPorTechnicalId(String processKey) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado con technical_id: " + processKey));
        return toDto(domain);
    }

    public List<BpmnProcessDesignDTO> listarTodos() {
        return designPort.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * FIX BUG-UAT-M5-01: Activa un proceso en BD cambiando su status a ACTIVE.
     * Invocado por POST /deploy tras un despliegue exitoso para que el proceso
     * aparezca en GET /catalog?status=ACTIVE (Portal del Operario).
     * Transición: DRAFT → PENDING_DEPLOY → ACTIVE (respeta máquina de estados del dominio).
     */
    public void activarProcesoPorTechnicalId(String processKey) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado: " + processKey));
        // La máquina de estados requiere: DRAFT → requestDeploy() → PENDING_DEPLOY → deploy() → ACTIVE
        if (domain.getStatus() == BpmnProcessDesign.Status.DRAFT) {
            domain.requestDeploy();
        }
        if (domain.getStatus() == BpmnProcessDesign.Status.PENDING_DEPLOY) {
            domain.deploy();
        }
        designPort.save(domain);
    }

    public void archivar(UUID id, String userId) {
        BpmnProcessDesign domain = getDomainModel(id);
        domain.archive();
        designPort.save(domain);

        auditPort.logAction(id, "ARCHIVE", userId, domain.getCurrentVersion(), null);
    }

    // --- Auto-Save (CA-10) ---

    /**
     * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
     * Auto-Save del Borrador (XML) en base de datos.
     */
    @Traceability(US = "US-005", CA = {"CA-10"})
    public void guardarBorrador(UUID id, String xml, String userId) {
        BpmnProcessDesign domain = getDomainModel(id);
        domain.updateDraft(xml);
        designPort.save(domain);

        auditPort.logAction(id, "SAVE_DRAFT", userId, domain.getCurrentVersion(), null);
    }

    // @Traceability: US-005, CA-15
    @Traceability(US = "US-005", CA = {"CA-10"})
    public void guardarBorradorPorTechnicalId(String processKey, String xml, String userId) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseGet(() -> {
                    // Si el proceso es nuevo, lo creamos automáticamente en estado BORRADOR con versión 0
                    String processName = capitalizeTechnicalId(processKey);
                    return BpmnProcessDesign.crear(
                            processName,
                            processKey,
                            BpmnProcessDesign.FormPattern.SIMPLE,
                            userId
                    );
                });
        domain.updateDraft(xml);
        designPort.save(domain);

        auditPort.logAction(domain.getId(), "SAVE_DRAFT", userId, domain.getCurrentVersion(), null);
    }

    // @Traceability: US-005, CA-15
    private String capitalizeTechnicalId(String slug) {
        if (slug == null || slug.isEmpty()) return "Proceso Sin Título";
        return java.util.Arrays.stream(slug.split("-"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    // --- Configuración de Generic Form (CA-7) ---

    public void updateGenericFormConfig(String processKey, String whitelistJson, String userId) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado con technical_id: " + processKey));
        
        domain.updateGenericFormConfig(whitelistJson);
        designPort.save(domain);

        auditPort.logAction(domain.getId(), "EDIT", userId, domain.getCurrentVersion(), "{\"event\":\"UPDATE_GENERIC_FORM_WHITELIST\"}");
    }

    // --- Request Deploy (CA-69) ---

    /**
     * @Traceability: US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
     * CA-69: Request de despliegue con versionamiento y revisión.
     */
    @Traceability(US = "US-005", CA = {"CA-69"})
    public Map<String, String> createDeployRequest(String processKey, String requestedBy, String xmlPayload) {
        // Validación 1: El proceso debe existir y estar en estado DRAFT o ACTIVE
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado con technical_id: " + processKey));
        
        domain.requestDeploy();
        designPort.save(domain);

        DeployRequestPort.DeployRequestInfo info = new DeployRequestPort.DeployRequestInfo(
            UUID.randomUUID(), processKey, requestedBy, LocalDateTime.now(), "PENDING", null, null, null, xmlPayload
        );
        deployRequestPort.save(info);
        
        auditPort.logAction(domain.getId(), "REQUEST_DEPLOY", requestedBy, domain.getCurrentVersion(), null);

        return Map.of(
            "message", "Solicitud de despliegue creada con éxito.",
            "status", "PENDING_APPROVAL",
            "processKey", processKey
        );
    }

    public Map<String, String> approveDeployRequest(UUID requestId, String adminUser, String comment) {
        DeployRequestPort.DeployRequestInfo request = deployRequestPort.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Deploy request no encontrado."));

        if (!"PENDING".equals(request.status())) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes PENDING.");
        }

        DeployRequestPort.DeployRequestInfo approved = new DeployRequestPort.DeployRequestInfo(
            request.id(), request.processDefinitionKey(), request.requestedBy(), request.requestedAt(),
            "APPROVED", adminUser, LocalDateTime.now(), comment, request.xmlPayload()
        );
        deployRequestPort.save(approved);

        BpmnProcessDesign domain = designPort.findByTechnicalId(request.processDefinitionKey())
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado."));
        
        domain.deploy();
        designPort.save(domain);

        auditPort.logAction(domain.getId(), "DEPLOY_APPROVED", adminUser, domain.getCurrentVersion(), "{\"comment\":\"" + comment + "\"}");

        return Map.of(
            "message", "Despliegue aprobado y versión incrementada.",
            "status", "APPROVED",
            "newVersion", String.valueOf(domain.getCurrentVersion())
        );
    }

    public Map<String, String> rejectDeployRequest(UUID requestId, String adminUser, String comment) {
        if (comment == null || comment.trim().length() < 20) {
            throw new IllegalArgumentException("El comentario de rechazo debe tener al menos 20 caracteres (CA-69).");
        }
        DeployRequestPort.DeployRequestInfo request = deployRequestPort.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Deploy request no encontrado."));

        if (!"PENDING".equals(request.status())) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes PENDING.");
        }

        DeployRequestPort.DeployRequestInfo rejected = new DeployRequestPort.DeployRequestInfo(
            request.id(), request.processDefinitionKey(), request.requestedBy(), request.requestedAt(),
            "REJECTED", adminUser, LocalDateTime.now(), comment, request.xmlPayload()
        );
        deployRequestPort.save(rejected);

        BpmnProcessDesign domain = designPort.findByTechnicalId(request.processDefinitionKey())
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado."));
        
        // Revertir estado a DRAFT o ACTIVE (en este caso lo volvemos a DRAFT para simplificar, o lo dejamos como antes)
        
        auditPort.logAction(domain.getId(), "DEPLOY_REJECTED", adminUser, domain.getCurrentVersion(), "{\"comment\":\"" + comment + "\"}");

        return Map.of(
            "message", "Despliegue rechazado.",
            "status", "REJECTED"
        );
    }

    public java.util.List<DeployRequestPort.DeployRequestInfo> getDeployRequests(String processKey) {
        return deployRequestPort.findByProcessKey(processKey);
    }

    // --- Lock Pesimista Separado (CA-66, CA-64) ---

    // @Traceability: US-005, CA-16
    public java.util.Optional<com.ibpms.poc.application.port.out.ProcessLockPort.ProcessLockInfo> getLockInfo(String processKey) {
        cleanStaleLock(processKey);
        return processLockPort.findLock(processKey);
    }

    public void acquireLockTechnicalKey(String processKey, String userId, String browserSessionId) {
        cleanStaleLock(processKey);
        processLockPort.findLock(processKey).ifPresent(lock -> {
            if (!lock.lockedBy().equals(userId)) {
                throw new IllegalStateException("El proceso ya se encuentra bloqueado por otro usuario: " + lock.lockedBy());
            }
        });
        processLockPort.saveLock(processKey, userId, browserSessionId);
        auditByTechnicalId(processKey, "LOCK", userId, "{\"session\": \"" + browserSessionId + "\"}");
    }

    public void heartbeatLock(String processKey, String userId) {
        cleanStaleLock(processKey);
        ProcessLockPort.ProcessLockInfo lock = processLockPort.findLock(processKey)
                .orElseThrow(() -> new IllegalStateException("No tienes un bloqueo activo sobre este proceso."));
        if (!lock.lockedBy().equals(userId)) {
            throw new IllegalStateException("Este proceso está bloqueado por otro usuario.");
        }
        processLockPort.saveLock(processKey, userId, lock.browserSessionId());
    }

    public void releaseLockTechnicalKey(String processKey, String userId) {
        processLockPort.findLock(processKey).ifPresent(lock -> {
            if (lock.lockedBy().equals(userId)) {
                processLockPort.deleteLock(processKey);
                auditByTechnicalId(processKey, "UNLOCK", userId, "{\"type\": \"normal\"}");
            }
        });
    }

    public void forceReleaseLock(String processKey, String adminUserId) {
        processLockPort.findLock(processKey).ifPresent(lock -> {
            processLockPort.deleteLock(processKey);
            auditByTechnicalId(processKey, "UNLOCK", adminUserId, "{\"type\": \"forced\", \"previousOwner\": \"" + lock.lockedBy() + "\"}");
            try {
                jdbcTemplate.update(
                    "INSERT INTO ibpms_audit_log (id, entity_type, entity_id, event_type, performed_by, event_data, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?::json, ?)",
                    UUID.randomUUID().toString(),
                    "BPMN_PROCESS",
                    processKey,
                    "FORCE_UNLOCK",
                    adminUserId,
                    "{\"action\": \"force_unlock\", \"previousOwner\": \"" + lock.lockedBy() + "\"}",
                    java.sql.Timestamp.valueOf(LocalDateTime.now())
                );
            } catch (Exception e) {
                // Fail-safe to prevent breaking lock deletion on audit failure
            }
        });
    }

    private void cleanStaleLock(String processKey) {
        processLockPort.findLock(processKey).ifPresent(lock -> {
            if (lock.lockedAt().isBefore(LocalDateTime.now().minusSeconds(90))) {
                processLockPort.deleteLock(processKey);
                auditByTechnicalId(processKey, "UNLOCK", "SYSTEM", "{\"type\": \"stale_timeout\", \"previousOwner\": \"" + lock.lockedBy() + "\"}");
            }
        });
    }

    private void auditByTechnicalId(String processKey, String action, String userId, String details) {
        designPort.findByTechnicalId(processKey).ifPresent(domain -> {
            auditPort.logAction(domain.getId(), action, userId, domain.getCurrentVersion(), details);
        });
    }

    // --- Helpers ---

    public BpmnProcessDesign getDomainModel(UUID id) {
        return designPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado: " + id));
    }

    // --- Mappers manuales ---

    private BpmnProcessDesignDTO toDto(BpmnProcessDesign e) {
        BpmnProcessDesignDTO dto = new BpmnProcessDesignDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setTechnicalId(e.getTechnicalId());
        dto.setFormPattern(e.getFormPattern() != null ? e.getFormPattern().name() : null);
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
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

    // @Traceability: US-005, CA-42 - Activity Timeline
    public List<BpmnDesignAuditLogDTO> getAuditLogsForProcess(String processKey) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseThrow(() -> new IllegalArgumentException("Diseño BPMN no encontrado con technical_id: " + processKey));
        
        return auditPort.getAuditLogsForProcess(domain.getId()).stream()
                .map(entry -> {
                    BpmnDesignAuditLogDTO dto = new BpmnDesignAuditLogDTO();
                    dto.setId(entry.getId());
                    dto.setAction(mapDomainActionToFrontendAction(entry.getAction().name()));
                    dto.setUserId(entry.getUserId());
                    dto.setTimestamp(entry.getTimestamp());
                    dto.setVersionAffected(entry.getVersionAffected());
                    dto.setDetails(entry.getDetails());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String mapDomainActionToFrontendAction(String domainAction) {
        if (domainAction == null) return "";
        switch (domainAction) {
            case "IMPORT":
            case "SAVE_DRAFT":
                return "IMPORT XML";
            case "DEPLOY":
            case "DEPLOY_APPROVED":
                return "DEPLOYED";
            case "REQUEST_DEPLOY":
                return "REQUEST DEPLOY";
            case "ARCHIVE":
                return "ARCHIVED";
            case "ROLLBACK":
                return "ROLLBACK";
            default:
                return domainAction;
        }
    }
}
