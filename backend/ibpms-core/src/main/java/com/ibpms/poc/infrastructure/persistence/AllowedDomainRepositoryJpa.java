// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.domain.port.AllowedDomainRepository;
import com.ibpms.poc.infrastructure.jpa.entity.AllowedDomainJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.AllowedDomainMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AllowedDomainRepositoryJpa implements AllowedDomainRepository {

    private final SpringDataAllowedDomainRepo springDataRepo;
    private final AllowedDomainMapper mapper;

    public AllowedDomainRepositoryJpa(SpringDataAllowedDomainRepo springDataRepo, AllowedDomainMapper mapper) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
    }

    @Override
    public AllowedDomain save(AllowedDomain domain) {
        AllowedDomainJpaEntity entity = mapper.toEntity(domain);
        AllowedDomainJpaEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AllowedDomain> findById(UUID id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AllowedDomain> findByTenantIdAndIsActiveTrue(String tenantId) {
        return springDataRepo.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDomainAndTenantIdAndIsActiveTrue(String domain, String tenantId) {
        return springDataRepo.existsByDomainAndTenantIdAndIsActiveTrue(domain, tenantId);
    }

    @Override
    public void deleteAll() {
        springDataRepo.deleteAll();
    }
}

interface SpringDataAllowedDomainRepo extends JpaRepository<AllowedDomainJpaEntity, UUID> {
    List<AllowedDomainJpaEntity> findByTenantIdAndIsActiveTrue(String tenantId);
    boolean existsByDomainAndTenantIdAndIsActiveTrue(String domain, String tenantId);
}
