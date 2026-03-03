package com.ibpms.poc.application.service.sgdea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataMappingTest {

    private final WebClient.Builder mockBuilder = WebClient.builder();
    private final SharePointAdapterService adapter = new SharePointAdapterService(mockBuilder);

    // ── QA Instruction: Large Stream Uploads (50MB OOM Guard) ──
    @Test
    @DisplayName("Debe leer un archivo temporal de >50MB desde disco duro y transmitirlo a SharePoint sin arrojar OutOfMemoryError")
    void uploadMassiveFileStream_DoesNotCauseOom_On50MB() throws IOException {

        // 1. Arrange: Crear un dummy file temporal masivo (50 MB)
        File tempFile = File.createTempFile("ibpms_payload_mock_50mb", ".pdf");
        tempFile.deleteOnExit();

        byte[] chunk = new byte[1024 * 1024]; // 1 MB buffer
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            for (int i = 0; i < 50; i++) {
                fos.write(chunk); // Fill the disk file slowly
            }
        }

        assertTrue(tempFile.length() >= 50 * 1024 * 1024L, "El archivo debe pesar al menos 50MB");

        // WebFlux DataBuffer envoltorio
        FileSystemResource streamableResource = new FileSystemResource(tempFile);

        // 2 & 3. Act & Assert: Transmitir archivo. NO debe tronar la memoria.
        assertDoesNotThrow(() -> {
            adapter.uploadMassiveFileStream("SITE_XYZ", "documentoLegal.pdf", streamableResource);
        });

        // La protección radica en que WebFlux + Reactor Netty leen FileSystemResource
        // byte por byte sin hacer Arrays Masivos (byte[]) protegiendo el Garbage
        // Collector (OOM).
    }
}
