package com.ibpms.poc.application.service.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.IdentityEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.IdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IdentityService {

    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<IdentityEntity> findAll() {
        return identityRepository.findAll();
    }
}
