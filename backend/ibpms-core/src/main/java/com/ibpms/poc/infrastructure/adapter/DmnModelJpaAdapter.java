// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.domain.model.DmnModel;
import com.ibpms.poc.domain.port.DmnModelRepositoryPort;
import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelJpaEntity;
import com.ibpms.poc.infrastructure.jpa.mapper.DmnModelMapper;
import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DmnModelJpaAdapter implements DmnModelRepositoryPort {

    private final DmnModelRepository dmnModelRepository;
    private final DmnModelMapper dmnModelMapper;

    public DmnModelJpaAdapter(DmnModelRepository dmnModelRepository, DmnModelMapper dmnModelMapper) {
        this.dmnModelRepository = dmnModelRepository;
        this.dmnModelMapper = dmnModelMapper;
    }

    @Override
    public Optional<DmnModel> findById(String id) {
        return dmnModelRepository.findById(id)
                .map(dmnModelMapper::toDomain);
    }

    @Override
    public DmnModel save(DmnModel dmnModel) {
        DmnModelJpaEntity entity = dmnModelMapper.toEntity(dmnModel);
        DmnModelJpaEntity savedEntity = dmnModelRepository.save(entity);
        return dmnModelMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(DmnModel dmnModel) {
        DmnModelJpaEntity entity = dmnModelMapper.toEntity(dmnModel);
        dmnModelRepository.delete(entity);
    }

    @Override
    public List<DmnModel> findByTenantId(String tenantId) {
        return dmnModelRepository.findByTenantId(tenantId)
                .stream()
                .map(dmnModelMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DmnModel> findByStatusAndUpdatedAtBefore(String status, LocalDateTime cutoff) {
        return dmnModelRepository.findByStatusAndUpdatedAtBefore(status, cutoff)
                .stream()
                .map(dmnModelMapper::toDomain)
                .collect(Collectors.toList());
    }
}
