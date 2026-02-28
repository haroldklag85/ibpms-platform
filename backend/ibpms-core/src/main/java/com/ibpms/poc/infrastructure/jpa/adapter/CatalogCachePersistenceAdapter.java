package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.CatalogCachePort;
import com.ibpms.poc.infrastructure.jpa.entity.CatalogCacheEntity;
import com.ibpms.poc.infrastructure.jpa.repository.CatalogCacheRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CatalogCachePersistenceAdapter implements CatalogCachePort {

    private final CatalogCacheRepository repository;

    public CatalogCachePersistenceAdapter(CatalogCacheRepository repository) {
        this.repository = repository;
    }

    @Override
    public void updateCache(String catalogId, String payload) {
        CatalogCacheEntity entity = repository.findById(catalogId)
                .orElseGet(() -> new CatalogCacheEntity(catalogId, payload, LocalDateTime.now()));

        entity.setPayload(payload);
        entity.setLastSyncAt(LocalDateTime.now());

        repository.save(entity);
    }

    @Override
    public Optional<String> getCatalogFallback(String catalogId) {
        return repository.findById(catalogId)
                .map(CatalogCacheEntity::getPayload);
    }
}
