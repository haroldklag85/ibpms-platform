package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.CatalogCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.lang.NonNull;

@Repository
public interface CatalogCacheRepository extends JpaRepository<CatalogCacheEntity, String> {
    @NonNull
    Optional<CatalogCacheEntity> findById(@NonNull String id);
}
