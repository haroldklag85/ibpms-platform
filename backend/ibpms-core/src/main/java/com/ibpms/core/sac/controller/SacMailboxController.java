package com.ibpms.core.sac.controller;

import com.ibpms.core.sac.domain.SacMailbox;
import com.ibpms.core.sac.dto.CreateSacMailboxDTO;
import com.ibpms.core.sac.dto.SacMailboxDTO;
import com.ibpms.core.sac.repository.SacMailboxRepository;
import com.ibpms.core.sac.service.AzureKeyVaultClient;
import com.ibpms.core.sac.service.MailboxConnectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/mailboxes")
public class SacMailboxController {

    private final SacMailboxRepository repository;
    private final MailboxConnectionManager connectionManager;
    private final AzureKeyVaultClient keyVaultClient;

    public SacMailboxController(SacMailboxRepository repository,
            MailboxConnectionManager connectionManager,
            AzureKeyVaultClient keyVaultClient) {
        this.repository = repository;
        this.connectionManager = connectionManager;
        this.keyVaultClient = keyVaultClient;
    }

    @GetMapping
    public List<SacMailboxDTO> listMailboxes() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConnection(@RequestBody CreateSacMailboxDTO dto) {
        if ("GRAPH".equalsIgnoreCase(dto.getProtocol())) {
            connectionManager.validateGraphConnection(dto.getTenantId(), dto.getClientId(), dto.getClientSecret());
            return ResponseEntity.ok("Conexión MS Graph exitosa");
        }
        return ResponseEntity.badRequest().body("Protocolo no soportado para test");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SacMailboxDTO createMailbox(@RequestBody CreateSacMailboxDTO dto) {

        // 1. Validacion de Ping en Vivo (Test de conexion)
        if ("GRAPH".equalsIgnoreCase(dto.getProtocol())) {
            connectionManager.validateGraphConnection(dto.getTenantId(), dto.getClientId(), dto.getClientSecret());
        }

        // 2. Almacenar secret en Azure Key Vault
        String secretName = "MboxSecret-" + dto.getAlias();
        String kvRefId = keyVaultClient.storeSecret(secretName, dto.getClientSecret());

        // 3. Crear entidad estéril en BD Relacional (Cumpliendo Regla CA-1)
        SacMailbox mailbox = new SacMailbox();
        mailbox.setAlias(dto.getAlias());
        mailbox.setTenantId(dto.getTenantId());
        mailbox.setClientId(dto.getClientId());
        mailbox.setProtocol(SacMailbox.MailboxProtocol.valueOf(dto.getProtocol().toUpperCase()));
        mailbox.setDefaultBpmnProcessId(dto.getDefaultBpmnProcessId());
        mailbox.setKeyVaultReferenceId(kvRefId);
        mailbox.setActive(true);

        SacMailbox saved = repository.save(mailbox);

        return toDto(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable String id, @RequestParam boolean active) {
        SacMailbox mailbox = repository.findById(java.util.Objects.requireNonNull(id)).orElseThrow();
        mailbox.setActive(active);
        repository.save(mailbox);
        return ResponseEntity.noContent().build();
    }

    private SacMailboxDTO toDto(SacMailbox m) {
        SacMailboxDTO dto = new SacMailboxDTO();
        dto.setId(m.getId());
        dto.setAlias(m.getAlias());
        dto.setTenantId(m.getTenantId());
        dto.setClientId(m.getClientId());
        dto.setProtocol(m.getProtocol().name());
        dto.setActive(m.isActive());
        dto.setDefaultBpmnProcessId(m.getDefaultBpmnProcessId());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
