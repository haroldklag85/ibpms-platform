package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.port.out.DeployRequestPort;
import com.ibpms.poc.infrastructure.jpa.entity.DeployRequestEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DeployRequestRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DeployRequestJpaAdapter implements DeployRequestPort {

    private final DeployRequestRepository repository;

    public DeployRequestJpaAdapter(DeployRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<DeployRequestInfo> findById(UUID id) {
        return repository.findById(id)
                .map(e -> new DeployRequestInfo(
                        e.getId(),
                        e.getProcessDefinitionKey(),
                        e.getRequestedBy(),
                        e.getRequestedAt(),
                        e.getStatus() != null ? e.getStatus().name() : null,
                        e.getReviewedBy(),
                        e.getReviewedAt(),
                        e.getReviewComment(),
                        e.getXmlPayload()
                ));
    }

    @Override
    public DeployRequestInfo save(DeployRequestInfo info) {
        DeployRequestEntity entity = new DeployRequestEntity();
        entity.setId(info.id());
        entity.setProcessDefinitionKey(info.processDefinitionKey());
        entity.setRequestedBy(info.requestedBy());
        entity.setRequestedAt(info.requestedAt());
        entity.setStatus(info.status() != null ? DeployRequestEntity.Status.valueOf(info.status()) : null);
        entity.setReviewedBy(info.reviewedBy());
        entity.setReviewedAt(info.reviewedAt());
        entity.setReviewComment(info.reviewComment());
        entity.setXmlPayload(info.xmlPayload());
        
        DeployRequestEntity saved = repository.save(entity);
        return new DeployRequestInfo(
                saved.getId(),
                saved.getProcessDefinitionKey(),
                saved.getRequestedBy(),
                saved.getRequestedAt(),
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getReviewedBy(),
                saved.getReviewedAt(),
                saved.getReviewComment(),
                saved.getXmlPayload()
        );
    }

    @Override
    public java.util.List<DeployRequestInfo> findByProcessKey(String processKey) {
        return repository.findByProcessDefinitionKeyOrderByRequestedAtDesc(processKey)
                .stream()
                .map(e -> new DeployRequestInfo(
                        e.getId(),
                        e.getProcessDefinitionKey(),
                        e.getRequestedBy(),
                        e.getRequestedAt(),
                        e.getStatus() != null ? e.getStatus().name() : null,
                        e.getReviewedBy(),
                        e.getReviewedAt(),
                        e.getReviewComment(),
                        e.getXmlPayload()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
