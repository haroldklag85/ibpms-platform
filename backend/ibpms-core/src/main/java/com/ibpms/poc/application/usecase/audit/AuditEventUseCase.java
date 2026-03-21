package com.ibpms.poc.application.usecase.audit;

import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAnomalyEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.SecurityAnomalyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Port-In (UseCase) Hexagonal para asentar Eventos de Auditoría de forma Inmutable.
 */
@Service
public class AuditEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuditEventUseCase.class);
    private final SecurityAnomalyRepository securityAnomalyRepository;

    public AuditEventUseCase(SecurityAnomalyRepository securityAnomalyRepository) {
        this.securityAnomalyRepository = securityAnomalyRepository;
    }

    /**
     * CA-10: Auditoría de Lectura de Secretos (SECRETS_VIEWED).
     * Ejecuta inyección Asíncrona (Fire & Forget) para garantizar 200 OK Fast.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerSecretViewed(String userId, String resourceId, String reason, String ipAddress) {
        log.warn("[AUDIT SECRETS] Transparencia Absoluta. Usuario [{}] (IP: {}) visualizó secreto vinculado al Recurso: [{}]. Motivo: {}", 
                 userId, ipAddress, resourceId, reason);

        // Se reutiliza la tabla forense inmutable para trazar este evento de alta criticidad.
        SecurityAnomalyEntity auditTrail = new SecurityAnomalyEntity(
                "SECRETS_VIEWED",
                userId,
                resourceId + " | IP: " + ipAddress + " | Reason: " + reason,
                LocalDateTime.now(),
                "RESOLVED" // Por ser log de lectura pasivo, ya nace cerrado sin requerir a un CISO.
        );
        
        securityAnomalyRepository.save(auditTrail);
    }
}
