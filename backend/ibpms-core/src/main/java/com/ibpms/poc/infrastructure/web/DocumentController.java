package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.DocumentoDTO;
import com.ibpms.poc.application.port.in.ListarDocumentosUseCase;
import com.ibpms.poc.application.port.in.GenerarPdfOficialUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Adaptador Driving: Auditar y listar Bóveda de Documentos (SGDEA) y Generación
 * Actas.
 */
@RestController
@RequestMapping("/api/v1/cases")
public class DocumentController {

    private final ListarDocumentosUseCase listarDocumentosUseCase;
    private final GenerarPdfOficialUseCase generarPdfOficialUseCase;

    public DocumentController(ListarDocumentosUseCase listarDocumentosUseCase,
            GenerarPdfOficialUseCase generarPdfOficialUseCase) {
        this.listarDocumentosUseCase = listarDocumentosUseCase;
        this.generarPdfOficialUseCase = generarPdfOficialUseCase;
    }

    /**
     * Endpoint API Rest - GET /api/v1/cases/{caseId}/documents
     * Retorna el arbol de documentos indexados al expediente y hosteados en Azure
     * Storage Blob
     */
    @GetMapping("/{caseId}/documents")
    public ResponseEntity<List<DocumentoDTO>> getDocumentsByCase(@PathVariable String caseId) {
        List<DocumentoDTO> documents = listarDocumentosUseCase.listarPorCaso(caseId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Endpoint API Rest - POST /api/v1/cases/{caseId}/generate-pdf
     * Generador PDF (DMN/Payload to PDF) manual o interaccionable.
     */
    @PostMapping("/{caseId}/generate-pdf")
    public ResponseEntity<Void> generatePdf(
            @PathVariable String caseId,
            @RequestBody Map<String, Object> payload) {

        // Autenticación oficial si está presente
        String author = "System / API Call";
        if (SecurityContextHolder.getContext() != null &&
                SecurityContextHolder.getContext().getAuthentication() != null) {
            author = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        generarPdfOficialUseCase.generarPdfCierre(caseId, payload, author);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
