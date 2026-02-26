package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.IdempotencyPort;
import com.ibpms.poc.infrastructure.jpa.entity.IdempotencyKeyEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador Driven — Idempotencia (JPA).
 * Implementa IdempotencyPort usando la tabla ibpms_idempotency_key.
 */
@Component
public class IdempotencyJpaAdapter implements IdempotencyPort {

    private final IdempotencyKeyRepository repository;

    public IdempotencyJpaAdapter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String idempotencyKey) {
        return repository.existsById(idempotencyKey);
    }

    @Override
    @Transactional
    public void registrar(String idempotencyKey, String resultadoJson) {
        var entity = new IdempotencyKeyEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setResultJson(resultadoJson);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerResultado(String idempotencyKey) {
        return repository.findById(idempotencyKey)
                .map(IdempotencyKeyEntity::getResultJson)
                .orElse(null);
    }
}
