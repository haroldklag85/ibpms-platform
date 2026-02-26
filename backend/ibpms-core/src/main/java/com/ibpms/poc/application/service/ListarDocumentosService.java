package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DocumentoDTO;
import com.ibpms.poc.application.port.in.ListarDocumentosUseCase;
import com.ibpms.poc.application.port.out.DocumentoRepositoryPort;
import com.ibpms.poc.application.port.out.DocumentStoragePort;
import com.ibpms.poc.domain.model.Documento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListarDocumentosService implements ListarDocumentosUseCase {

    private final DocumentoRepositoryPort documentoRepositoryPort;
    private final DocumentStoragePort documentStoragePort;

    public ListarDocumentosService(DocumentoRepositoryPort documentoRepositoryPort,
            DocumentStoragePort documentStoragePort) {
        this.documentoRepositoryPort = documentoRepositoryPort;
        this.documentStoragePort = documentStoragePort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDTO> listarPorCaso(String caseId) {
        List<Documento> documentos = documentoRepositoryPort.findByCaseId(caseId);
        return documentos.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private DocumentoDTO toDto(Documento doc) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setId(doc.getId().toString());
        dto.setCaseId(doc.getCaseId().toString());
        dto.setDocumentTypeCode(doc.getDocumentTypeCode());
        dto.setFileName(doc.getFileName());

        // Emisión de URL Segura y Temporal (Ej. SAS Token) en vez de ruta absoluta
        // vulnerable
        dto.setBlobUri(documentStoragePort.generateSecureUrl(doc.getBlobUri()));

        dto.setUploadedBy(doc.getUploadedBy());
        if (doc.getUploadedAt() != null) {
            dto.setUploadedAt(doc.getUploadedAt().toString());
        }
        return dto;
    }
}
