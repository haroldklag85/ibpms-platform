package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.OutboundConfigEntity;
import com.ibpms.poc.infrastructure.jpa.repository.OutboundConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OutboundConfigService {

    private final OutboundConfigRepository outboundConfigRepository;

    public OutboundConfigService(OutboundConfigRepository outboundConfigRepository) {
        this.outboundConfigRepository = outboundConfigRepository;
    }

    // @Traceability: Retro-Remediación ADR-001
    public List<OutboundConfigEntity> findAll() {
        return outboundConfigRepository.findAll();
    }

    // @Traceability: Retro-Remediación ADR-001
    public OutboundConfigEntity saveOutboundConfig(OutboundConfigEntity entity) {
        return outboundConfigRepository.save(entity);
    }
}
