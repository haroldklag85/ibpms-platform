package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.event.SecurityAnomalyEvent;
import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAnomalyEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.SecurityAnomalyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SecurityAnomalyListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityAnomalyListener.class);
    private final SecurityAnomalyRepository securityAnomalyRepository;

    public SecurityAnomalyListener(SecurityAnomalyRepository securityAnomalyRepository) {
        this.securityAnomalyRepository = securityAnomalyRepository;
    }

    /**
     * CA-11: Desacoplamiento de Rollback. 
     * Las anomalías se insertan en un Hilo separado con REQUIRES_NEW.
     * Si la API central hace abort (Ej: por el SoD), el logger persiste.
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSecurityAnomalyEvent(SecurityAnomalyEvent event) {
        log.error("[SECURITY ANOMALY ALARM] Type: {}, User: {}, Resource: {}. Persistiendo Forensics Inmutables DB.", 
                  event.getType(), event.getUserId(), event.getResourceId());
        
        SecurityAnomalyEntity anomaly = new SecurityAnomalyEntity(
                event.getType(),
                event.getUserId(),
                event.getResourceId(),
                LocalDateTime.now(),
                "OPEN"
        );
        securityAnomalyRepository.save(anomaly);
    }
}
