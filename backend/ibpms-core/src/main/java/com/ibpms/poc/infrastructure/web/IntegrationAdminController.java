package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.jpa.entity.InboundWebhookEntity;
import com.ibpms.poc.infrastructure.jpa.entity.OutboundConfigEntity;
import com.ibpms.poc.infrastructure.jpa.repository.InboundWebhookRepository;
import com.ibpms.poc.infrastructure.jpa.repository.OutboundConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/integrations")
public class IntegrationAdminController {

    private final InboundWebhookRepository inboundRepository;
    private final OutboundConfigRepository outboundRepository;

    public IntegrationAdminController(InboundWebhookRepository inboundRepository,
            OutboundConfigRepository outboundRepository) {
        this.inboundRepository = inboundRepository;
        this.outboundRepository = outboundRepository;
    }

    // --- Inbound Webhooks ---

    @GetMapping("/inbound")
    public ResponseEntity<List<InboundWebhookEntity>> getAllInboundWebhooks() {
        return ResponseEntity.ok(inboundRepository.findAll());
    }

    @PostMapping("/inbound")
    public ResponseEntity<InboundWebhookEntity> createInboundWebhook(@RequestBody InboundWebhookEntity entity) {
        return ResponseEntity.ok(inboundRepository.save(entity));
    }

    // --- Outbound Configs ---

    @GetMapping("/outbound")
    public ResponseEntity<List<OutboundConfigEntity>> getAllOutboundConfigs() {
        return ResponseEntity.ok(outboundRepository.findAll());
    }

    @PostMapping("/outbound")
    public ResponseEntity<OutboundConfigEntity> createOutboundConfig(@RequestBody OutboundConfigEntity entity) {
        return ResponseEntity.ok(outboundRepository.save(entity));
    }
}
