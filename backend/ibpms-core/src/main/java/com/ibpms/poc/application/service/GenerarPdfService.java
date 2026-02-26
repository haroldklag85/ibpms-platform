package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.GenerarPdfOficialUseCase;
import com.ibpms.poc.application.port.out.DocumentStoragePort;
import com.ibpms.poc.application.port.out.DocumentoRepositoryPort;
import com.ibpms.poc.domain.model.Documento;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class GenerarPdfService implements GenerarPdfOficialUseCase {

    private final DocumentStoragePort documentStoragePort;
    private final DocumentoRepositoryPort documentoRepositoryPort;

    public GenerarPdfService(DocumentStoragePort documentStoragePort, DocumentoRepositoryPort documentoRepositoryPort) {
        this.documentStoragePort = documentStoragePort;
        this.documentoRepositoryPort = documentoRepositoryPort;
    }

    @Override
    @Transactional
    public void generarPdfCierre(String caseId, Map<String, Object> variables, String author) {
        try {
            // 1. Crear PDF Estático (OpenPDF)
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document pdfDocument = new Document();
            PdfWriter.getInstance(pdfDocument, out);

            pdfDocument.open();
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font regularFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            pdfDocument.add(new Paragraph("CERTIFICADO DE APROBACIÓN DE EXPEDIENTE", titleFont));
            pdfDocument.add(new Paragraph(" "));
            pdfDocument.add(new Paragraph("ID Expediente: " + caseId, regularFont));
            pdfDocument.add(new Paragraph("Autorizador: " + author, regularFont));
            pdfDocument.add(new Paragraph("Fecha de Cierre: " + Instant.now().toString(), regularFont));
            pdfDocument.add(new Paragraph(" "));
            pdfDocument.add(new Paragraph("Variables Registradas en el Motor:", titleFont));

            variables.forEach((k, v) -> {
                try {
                    pdfDocument.add(new Paragraph("- " + k + ": " + v.toString(), regularFont));
                } catch (Exception ignored) {
                }
            });

            pdfDocument.close();

            byte[] pdfBytes = out.toByteArray();
            ByteArrayInputStream inStream = new ByteArrayInputStream(pdfBytes);

            // 2. Generar Hash Criptográfico (SHA-256)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pdfBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            String sha256 = hexString.toString();

            // 3. Subir al Blob Storage Azure (Azurite)
            String fileName = "Acta_Aprobacion_" + caseId + ".pdf";
            String blobUri = documentStoragePort.uploadFile(fileName, inStream, pdfBytes.length, "application/pdf");

            // 4. Registrar Entidad inmutable en JPA
            Documento docLegal = Documento.builder()
                    .caseId(UUID.fromString(caseId))
                    .documentTypeCode("ACTA_CIERRE_PDF")
                    .fileName(fileName)
                    .blobUri(blobUri)
                    .sha256Hash(sha256)
                    .uploadedBy(author)
                    .build();

            documentoRepositoryPort.save(docLegal);

        } catch (Exception e) {
            throw new RuntimeException("Error en Generación de Bóveda PDF", e);
        }
    }
}
