package com.ibpms.poc.application.service.sgdea;

import org.springframework.stereotype.Service;

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

    public void validateMagicBytes(InputStream inputStream, String originalFilename) throws IOException {
        byte[] magicBytes = new byte[4];
        if (inputStream.read(magicBytes, 0, 4) < 2) {
            throw new SecurityException("No se pudieron leer los Magic Bytes suficientes.");
        }

        // Verifica si es un archivo ejecutable DOS/Windows (Magic Bytes: 'MZ' -> 0x4D
        // 0x5A)
        if (magicBytes[0] == 0x4D && magicBytes[1] == 0x5A) {
            throw new SecurityException(
                    "¡Falsificación de MIME (Magic Bytes: MZ)! Archivo ejecutable malicioso detectado.");
        }

        // Verifica archivos marcados como .pdf
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
            // La firma binaria de PDF es '%PDF' -> 0x25 0x50 0x44 0x46
            if (magicBytes[0] != 0x25 || magicBytes[1] != 0x50 || magicBytes[2] != 0x44 || magicBytes[3] != 0x46) {
                throw new SecurityException(
                        "Falsificación de extensión detectada: El archivo indica ser .pdf pero sus Magic Bytes no corresponden a %PDF.");
            }
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

}
