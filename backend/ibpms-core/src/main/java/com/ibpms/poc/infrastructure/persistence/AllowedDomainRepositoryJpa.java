package com.ibpms.poc.infrastructure.persistence;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.domain.port.AllowedDomainRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AllowedDomainRepositoryJpa implements AllowedDomainRepository {

    private final SpringDataAllowedDomainRepo springDataRepo;

    public AllowedDomainRepositoryJpa(SpringDataAllowedDomainRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public AllowedDomain save(AllowedDomain domain) {
        return springDataRepo.save(domain);
    }

    @Override
    public Optional<AllowedDomain> findById(UUID id) {
        return springDataRepo.findById(id);
    }

    @Override
    public List<AllowedDomain> findByTenantIdAndIsActiveTrue(String tenantId) {
        return springDataRepo.findByTenantIdAndIsActiveTrue(tenantId);
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

interface SpringDataAllowedDomainRepo extends JpaRepository<AllowedDomain, UUID> {
    List<AllowedDomain> findByTenantIdAndIsActiveTrue(String tenantId);
    boolean existsByDomainAndTenantIdAndIsActiveTrue(String domain, String tenantId);
}
