package com.ibpms.poc.infrastructure.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.ibpms.poc.application.port.out.DocumentStoragePort;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador Driven para almacenar Documentos en Azure Blob Storage.
 * Configurado para apuntar a Azurite (emulador) en V1 según application.yml.
 */
@Component
@Primary
@Slf4j
public class AzureBlobStorageAdapter implements DocumentStoragePort {

    private final String connectionString;
    private final String containerName;
    private BlobContainerClient containerClient;

    public AzureBlobStorageAdapter(
            @Value("${app.azure.storage.connection-string}") String connectionString,
            @Value("${app.azure.storage.container-name}") String containerName) {
        this.connectionString = connectionString;
        this.containerName = containerName;
    }

    @PostConstruct
    public void init() {
        try {
            // Inicializar cliente y asegurar que el contenedor exista
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            this.containerClient = serviceClient.getBlobContainerClient(containerName);
            if (!this.containerClient.exists()) {
                this.containerClient.create();
            }
        } catch (Exception e) {
            log.warn("No se pudo conectar a Azure Blob Storage. La carga de documentos fallará: {}", e.getMessage());
        }
    }

    @Override
    public String saveDocument(java.util.UUID documentId, org.springframework.web.multipart.MultipartFile file) {
        return "";
    }

    @Override
    public String uploadFile(String fileName, InputStream data, long length, String mimeType) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        // Sobreescribe si existe
        blobClient.upload(data, length, true);
        return blobClient.getBlobUrl();
    }

    @Override
    public InputStream downloadFile(String blobUri) {
        // Extraemos solo el nombre del blob partiendo de la URI absoluta
        String blobName = blobUri.substring(blobUri.lastIndexOf('/') + 1);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.openInputStream();
    }

    @Override
    public String generateSecureUrl(String blobUri) {
        if (blobUri == null || blobUri.isBlank())
            return "";
        try {
            String blobName = blobUri.substring(blobUri.lastIndexOf('/') + 1);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Generar un token SAS de Solo-Lectura válido por 1 hora
            com.azure.storage.blob.sas.BlobSasPermission permission = new com.azure.storage.blob.sas.BlobSasPermission()
                    .setReadPermission(true);
            java.time.OffsetDateTime expiryTime = java.time.OffsetDateTime.now().plusHours(1);
            com.azure.storage.blob.sas.BlobServiceSasSignatureValues values = new com.azure.storage.blob.sas.BlobServiceSasSignatureValues(
                    expiryTime, permission)
                    .setStartTime(java.time.OffsetDateTime.now());

            return blobClient.getBlobUrl() + "?" + blobClient.generateSas(values);
        } catch (Exception e) {
            return blobUri; // Fallback a URL pública en caso de error
        }
    }
}
