package com.ibpms.core.sac.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Simulador de Integración con Azure Key Vault.
 * En un entorno real, usaría @AzureServiceClient o SecretClient.
 */
@Service
public class AzureKeyVaultClient {

    public String storeSecret(String secretName, String secretValue) {
        // Simular almacenamiento y retornar ID de referencia (Version Id o Key Name)
        return "kv-ref-" + UUID.randomUUID().toString();
    }

    public String getSecret(String referenceId) {
        // En este mock, solo devolvemos el ID.
        // En produccion iria a fetch al KeyVault.
        return "mocked-secret-for-" + referenceId;
    }
}
