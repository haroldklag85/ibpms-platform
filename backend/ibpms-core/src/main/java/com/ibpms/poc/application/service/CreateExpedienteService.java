package com.ibpms.poc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.application.port.in.CreateExpedienteUseCase;
import com.ibpms.poc.application.port.out.*;
import com.ibpms.poc.domain.model.Expediente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Aplicación — Crear Expediente.
 * Orquesta: idempotencia → dominio → Camunda → CRM → persistencia.
 */
@Service
public class CreateExpedienteService implements CreateExpedienteUseCase {

    private final ExpedienteRepositoryPort repositoryPort;
    private final ProcesoBpmPort procesoBpmPort;
    private final CrmClientPort crmClientPort;
    private final IdempotencyPort idempotencyPort;
    private final ObjectMapper objectMapper;

    public CreateExpedienteService(ExpedienteRepositoryPort repositoryPort,
            ProcesoBpmPort procesoBpmPort,
            CrmClientPort crmClientPort,
            IdempotencyPort idempotencyPort,
            ObjectMapper objectMapper) {
        this.repositoryPort = repositoryPort;
        this.procesoBpmPort = procesoBpmPort;
        this.crmClientPort = crmClientPort;
        this.idempotencyPort = idempotencyPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ExpedienteDTO create(ExpedienteDTO request) {
        // 1. Idempotencia: si ya existe la clave, devolver resultado previo
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null && idempotencyPort.existe(idempotencyKey)) {
            String previo = idempotencyPort.obtenerResultado(idempotencyKey);
            return deserializeDto(previo);
        }

        // 2. Enriquecer variables con metadatos del CRM (fail-graceful)
        var variables = request.getVariables() != null
                ? new java.util.HashMap<>(request.getVariables())
                : new java.util.HashMap<String, Object>();

        String clienteId = (String) variables.getOrDefault("clienteId", null);
        if (clienteId != null) {
            var metadatosCrm = crmClientPort.obtenerMetadatosCliente(clienteId);
            variables.putAll(metadatosCrm);
        }

        // 3. Crear entidad de dominio (pura, inmutable)
        Expediente expediente = Expediente.iniciarNuevo(
                request.getDefinitionKey(),
                request.getBusinessKey(),
                request.getType(),
                variables);

        // 4. Iniciar proceso en Camunda (compartiendo el mismo @Transactional)
        String processInstanceId = procesoBpmPort.iniciarProceso(
                request.getDefinitionKey(),
                request.getBusinessKey(),
                variables);
        expediente = expediente.vincularProceso(processInstanceId);

        // 5. Persistir el expediente enriquecido con el processInstanceId
        Expediente saved = repositoryPort.save(expediente);

        // 6. Construir DTO de respuesta
        ExpedienteDTO response = toDto(saved);

        // 7. Registrar idempotencia
        if (idempotencyKey != null) {
            idempotencyPort.registrar(idempotencyKey, serializeDto(response));
        }

        return response;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private ExpedienteDTO toDto(Expediente e) {
        ExpedienteDTO dto = new ExpedienteDTO();
        dto.setId(e.getId().toString());
        dto.setDefinitionKey(e.getDefinitionKey());
        dto.setBusinessKey(e.getBusinessKey());
        dto.setType(e.getType());
        dto.setStatus(e.getStatus().name());
        dto.setVariables(e.getVariables());
        dto.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        return dto;
    }

    private String serializeDto(ExpedienteDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private ExpedienteDTO deserializeDto(String json) {
        try {
            return objectMapper.readValue(json, ExpedienteDTO.class);
        } catch (Exception ex) {
            return new ExpedienteDTO();
        }
    }
}
