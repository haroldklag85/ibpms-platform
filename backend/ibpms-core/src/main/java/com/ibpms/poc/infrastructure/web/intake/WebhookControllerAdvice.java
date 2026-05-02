package com.ibpms.poc.infrastructure.web.intake;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.domain.port.OrphanPayloadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.UUID;

@ControllerAdvice(assignableTypes = WebhookIntakeController.class)
public class WebhookControllerAdvice {
    
    private final OrphanPayloadRepository orphanRepo;

    public WebhookControllerAdvice(OrphanPayloadRepository orphanRepo) {
        this.orphanRepo = orphanRepo;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        OrphanPayload orphan = OrphanPayload.builder()
                .id(UUID.randomUUID())
                .rawPayload("Unreadable body")
                .errorType("MALFORMED_JSON")
                .createdAt(ZonedDateTime.now())
                .build();
        orphanRepo.save(orphan);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MALFORMED_JSON: Body is not readable.");
    }
}
