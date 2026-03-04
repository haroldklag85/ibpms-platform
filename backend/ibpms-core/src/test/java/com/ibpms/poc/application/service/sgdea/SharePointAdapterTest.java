package com.ibpms.poc.application.service.sgdea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharePointAdapterTest {

    private final WebClient.Builder mockBuilder = WebClient.builder();
    private final SharePointAdapterService adapterService = new SharePointAdapterService(mockBuilder);

    // ── QA Instruction: Test SharePoint Adapter Auth & Bounds ──

    @Test
    @DisplayName("Debe rechazar la subida si el Token de EntraID no viene en formato Bearer o está ausente")
    void validateAuth_MissingOrInvalidToken_ThrowsSecurityException() {
        SecurityException ex1 = assertThrows(SecurityException.class,
                () -> adapterService.validateEntraIdTokenAndBounds(null, "https://graph.microsoft.com/v1.0/drives/1"));
        assertTrue(ex1.getMessage().contains("Falla de Autenticación"));

        SecurityException ex2 = assertThrows(SecurityException.class, () -> adapterService
                .validateEntraIdTokenAndBounds("Basic dXNlcjpwYXNz", "https://graph.microsoft.com/v1.0/drives/1"));
        assertTrue(ex2.getMessage().contains("Falla de Autenticación"));
    }

    @Test
    @DisplayName("Debe rechazar la subida si la URL de destino intenta escapar fuera de los límites de Graph API permitidos")
    void validateAuth_InvalidUrlBounds_ThrowsSecurityException() {
        SecurityException ex = assertThrows(SecurityException.class, () -> adapterService
                .validateEntraIdTokenAndBounds("Bearer validToken123456", "https://malicious-site.com/upload"));
        assertTrue(ex.getMessage().contains("Límites de Seguridad Excedidos"));
    }

    @Test
    @DisplayName("Debe permitir el flujo si trae token Bearer y apunta a un dominio autorizado de SharePoint/Graph")
    void validateAuth_ValidCredentialsAndBounds_Passes() {
        assertDoesNotThrow(() -> adapterService.validateEntraIdTokenAndBounds("Bearer mySecretEntraIdToken",
                "https://graph.microsoft.com/v1.0/drives/b!qwerty/root:/file.pdf"));
    }
}
