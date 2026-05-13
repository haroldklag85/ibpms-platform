package com.ibpms.poc.infrastructure.web.intake;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.application.service.intake.OrphanPayloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@ControllerAdvice(assignableTypes = WebhookIntakeController.class)
@ConditionalOnBean(OrphanPayloadService.class)
public class WebhookControllerAdvice {
    
    private final OrphanPayloadService orphanService;

    public WebhookControllerAdvice(OrphanPayloadService orphanService) {
        this.orphanService = orphanService;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        OrphanPayload orphan = OrphanPayload.builder()
                .id(UUID.randomUUID())
                .rawPayload("Unreadable body")
                .errorType("MALFORMED_JSON")
                .createdAt(ZonedDateTime.now())
                .build();
        // @Traceability: Retro-Remediación ADR-001
        orphanService.save(orphan);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MALFORMED_JSON: Body is not readable.");
    }
}
