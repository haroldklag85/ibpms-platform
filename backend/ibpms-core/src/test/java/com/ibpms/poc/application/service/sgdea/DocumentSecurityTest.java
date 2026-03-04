package com.ibpms.poc.application.service.sgdea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentSecurityTest {

    private final DocumentSecurityService documentSecurityService = new DocumentSecurityService();

    // ── QA Instruction: Test Document Security (Magic Bytes) ──
    @Test
    @DisplayName("Debe detectar un archivo ejecutable DOS/Windows falsificado como PDF (MIME Spoofing) y lanzar SecurityException")
    void validateMagicBytes_ExeSpoofedAsPdf_ThrowsSecurityException() {
        // "MZ" -> 0x4D, 0x5A es la firma binaria universal para ejecutables DOS/Windows
        // (exe, dll, sys)
        byte[] maliciousPayload = new byte[] { 0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00 };
        InputStream inputStream = new ByteArrayInputStream(maliciousPayload);

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            documentSecurityService.validateMagicBytes(inputStream, "factura_virus.pdf");
        });

        assertTrue(ex.getMessage().contains("Falsificación de MIME"));
        assertTrue(ex.getMessage().contains("MZ"));
    }

    @Test
    @DisplayName("Debe dejar pasar un PDF válido cuya firma binaria corresponda a '%PDF'")
    void validateMagicBytes_ValidPdf_Passes() throws IOException {
        // "%PDF" -> 0x25, 0x50, 0x44, 0x46
        byte[] validPdf = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34 };
        InputStream inputStream = new ByteArrayInputStream(validPdf);

        // Si es exitoso, no arroja ninguna excepción
        documentSecurityService.validateMagicBytes(inputStream, "documento.pdf");
    }
}
