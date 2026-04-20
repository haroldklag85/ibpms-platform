package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.UserDelegationEntity;
import com.ibpms.poc.infrastructure.jpa.repository.UserDelegationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DelegationService {

    private final UserDelegationRepository delegationRepository;

    public DelegationService(UserDelegationRepository delegationRepository) {
        this.delegationRepository = delegationRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasDelegation(String supervisorId, String assistantId, String tenantId) {
        Optional<UserDelegationEntity> delegation = delegationRepository.findBySupervisorIdAndAssistantIdAndTenantId(supervisorId, assistantId, tenantId);
        return delegation.isPresent();
    }
}
