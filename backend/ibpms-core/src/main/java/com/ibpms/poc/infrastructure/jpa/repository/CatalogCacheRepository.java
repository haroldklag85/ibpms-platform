package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.CatalogCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogCacheRepository extends JpaRepository<CatalogCacheEntity, String> {
    Optional<CatalogCacheEntity> findById(String id);
}
