package com.ibpms.poc.infrastructure.jpa.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibpms.poc.application.port.out.ExpedienteRepositoryPort;
import com.ibpms.poc.domain.model.Expediente;
import com.ibpms.poc.infrastructure.jpa.entity.ExpedienteEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ExpedienteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador Driven — JPA para Expediente.
 * Implementa ExpedienteRepositoryPort. Gestiona conversión Dominio ↔ Entidad
 * JPA.
 */
@Component
public class ExpedienteJpaAdapter implements ExpedienteRepositoryPort {

    private final ExpedienteRepository repository;
    private final ObjectMapper objectMapper;

    public ExpedienteJpaAdapter(ExpedienteRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Expediente save(Expediente expediente) {
        ExpedienteEntity entity = toEntity(expediente);
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Expediente> findById(UUID id) {
        return repository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Expediente> findByBusinessKey(String businessKey) {
        return repository.findByBusinessKey(businessKey).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expediente> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    // ── Conversión Dominio → Entidad ───────────────────────────────────────────
    private ExpedienteEntity toEntity(Expediente d) {
        ExpedienteEntity e = new ExpedienteEntity();
        e.setId(d.getId().toString());
        e.setDefinitionKey(d.getDefinitionKey());
        e.setBusinessKey(d.getBusinessKey());
        e.setType(d.getType());
        e.setStatus(d.getStatus().name());
        e.setProcessInstanceId(d.getProcessInstanceId());
        e.setCreatedAt(d.getCreatedAt());
        e.setPayload(toJson(d.getVariables()));
        return e;
    }

    // ── Conversión Entidad → Dominio ───────────────────────────────────────────
    private Expediente toDomain(ExpedienteEntity e) {
        return new Expediente.Builder()
                .id(UUID.fromString(e.getId()))
                .definitionKey(e.getDefinitionKey())
                .businessKey(e.getBusinessKey())
                .type(e.getType())
                .status(Expediente.ExpedienteStatus.valueOf(e.getStatus()))
                .processInstanceId(e.getProcessInstanceId())
                .variables(fromJson(e.getPayload()))
                .createdAt(e.getCreatedAt())
                .build();
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            if (json == null || json.isBlank())
                return Map.of();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return Map.of();
        }
    }
}
