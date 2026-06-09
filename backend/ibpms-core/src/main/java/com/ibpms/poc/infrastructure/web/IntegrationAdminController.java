package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import com.ibpms.poc.infrastructure.jpa.entity.OutboundConfigEntity;
import com.ibpms.poc.application.service.InboundWebhookService;
import com.ibpms.poc.application.service.OutboundConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/integrations")
@Traceability(US = "US-023", CA = {"CA-01"})
public class IntegrationAdminController {

    private final InboundWebhookService inboundService;
    private final OutboundConfigService outboundService;

    public IntegrationAdminController(InboundWebhookService inboundService,
            OutboundConfigService outboundService) {
        this.inboundService = inboundService;
        this.outboundService = outboundService;
    }

    // --- Inbound Webhooks ---

    @GetMapping("/inbound")
    public ResponseEntity<List<InboundWebhookEntity>> getAllInboundWebhooks() {
        // @Traceability: US-023 - CA-01 (ADR-001 Refactor)
        return ResponseEntity.ok(inboundService.findAll());
    }

    @PostMapping("/inbound")
    public ResponseEntity<InboundWebhookEntity> createInboundWebhook(@RequestBody InboundWebhookEntity entity) {
        // @Traceability: US-023 - CA-01 (ADR-001 Refactor)
        return ResponseEntity.ok(inboundService.saveInboundWebhook(java.util.Objects.requireNonNull(entity)));
    }

    // --- Outbound Configs ---

    @GetMapping("/outbound")
    public ResponseEntity<List<OutboundConfigEntity>> getAllOutboundConfigs() {
        // @Traceability: US-023 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(outboundService.findAll());
    }

    @PostMapping("/outbound")
    public ResponseEntity<OutboundConfigEntity> createOutboundConfig(@RequestBody OutboundConfigEntity entity) {
        // @Traceability: US-023 - CA-02 (ADR-001 Refactor)
        return ResponseEntity.ok(outboundService.saveOutboundConfig(java.util.Objects.requireNonNull(entity)));
    }
}
