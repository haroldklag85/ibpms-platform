package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.DocumentoRepositoryPort;
import com.ibpms.poc.domain.model.Documento;
import com.ibpms.poc.infrastructure.jpa.entity.DocumentoEntity;
import com.ibpms.poc.infrastructure.jpa.repository.DocumentoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DocumentoJpaAdapter implements DocumentoRepositoryPort {

    private final DocumentoJpaRepository repository;

    public DocumentoJpaAdapter(DocumentoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Documento documento) {
        DocumentoEntity entity = new DocumentoEntity();
        entity.setId(documento.getId().toString());
        entity.setCaseId(documento.getCaseId().toString());
        entity.setDocumentTypeCode(documento.getDocumentTypeCode());
        entity.setFileName(documento.getFileName());
        entity.setBlobUri(documento.getBlobUri());
        entity.setSha256Hash(documento.getSha256Hash());
        entity.setUploadedBy(documento.getUploadedBy());
        entity.setUploadedAt(documento.getUploadedAt());
        repository.save(entity);
    }

    @Override
    public List<Documento> findByCaseId(String caseId) {
        return repository.findByCaseIdOrderByUploadedAtDesc(caseId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Documento toDomain(DocumentoEntity entity) {
        return Documento.builder()
                .id(UUID.fromString(entity.getId()))
                .caseId(UUID.fromString(entity.getCaseId()))
                .documentTypeCode(entity.getDocumentTypeCode())
                .fileName(entity.getFileName())
                .blobUri(entity.getBlobUri())
                .sha256Hash(entity.getSha256Hash())
                .uploadedBy(entity.getUploadedBy())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}
