package com.ibpms.poc.application.service.sgdea;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Inspector Guardrails (CA-8, CA-12).
 * Valida MIME estricto (no basado solo en extensión) usando fragmentos
 * (Magic Numbers - simplificado para propósitos comunes) y calcula el Hash
 * transaccional SHA-256 in-flight (antes de almacenar a SharePoint).
 */
@Service
public class DocumentSecurityService {

    // Magic Numbers comunes (Hex Signatures)
    private static final byte[] MAGIC_PDF = { 0x25, 0x50, 0x44, 0x46 }; // %PDF
    private static final byte[] MAGIC_PNG = { (byte) 0x89, 0x50, 0x4E, 0x47 }; // PNG
    private static final byte[] MAGIC_JPEG_1 = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] MAGIC_ZIP_DOCX = { 0x50, 0x4B, 0x03, 0x04 }; // PK.. (DOCX is a zip)

    public void validateMimeRules(MultipartFile file) throws IOException, HttpMediaTypeNotSupportedException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no fue enviado.");
        }

        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            if (is.read(header) < 4) {
                throw new HttpMediaTypeNotSupportedException(
                        "Archivo corrupto o demasiado pequeño para inspección MIME.");
            }
        }

        boolean isValid = matchMagicNumber(header, MAGIC_PDF) ||
                matchMagicNumber(header, MAGIC_PNG) ||
                matchMagicNumber(header, MAGIC_JPEG_1) ||
                matchMagicNumber(header, MAGIC_ZIP_DOCX);

        if (!isValid) {
            throw new HttpMediaTypeNotSupportedException(
                    "Tipo de archivo bloqueado por directiva de Seguridad. Extensiones permitidas: PDF, PNG, JPG/JPEG, DOCX.");
        }
    }

    /**
     * Calcula SHA-256 al vuelo directamente desde el stream
     * evitando cargar archivos masivos a la RAM simuladamente.
     * En producción real esto absorbe chunks grandes (Ej. 8KB).
     */
    public String calculateInFlightSha256(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hashBytes = digest.digest();
        return HexFormat.of().formatHex(hashBytes);
    }

    private boolean matchMagicNumber(byte[] header, byte[] signature) {
        if (header.length < signature.length)
            return false;
        for (int i = 0; i < signature.length; i++) {
            if (header[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
