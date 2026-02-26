package com.ibpms.poc.application.port.out;

import java.io.InputStream;

/**
 * Puerto de Salida (Outbound Port)
 * Abstrae el comportamiento del almacenamiento físico de los documentos (Azure,
 * S3, FileSystem, etc).
 */
public interface DocumentStoragePort {

    /**
     * Sube un archivo al almacenamiento configurado.
     * 
     * @param fileName Nombre del archivo físico
     * @param data     Stream del archivo
     * @param length   Tamaño en bytes
     * @param mimeType Mime-Type (ej. application/pdf)
     * @return URL absoluta (Blob URI) para descargar u organizar el archivo.
     */
    String uploadFile(String fileName, InputStream data, long length, String mimeType);

    /**
     * Descarga el archivo como Array de Bytes (Podría optimizarse a Stream en un
     * futuro).
     * 
     * @param blobUri URL absoluta devuelta tras el upload.
     * @return Stream o Bytes (Para V1 usamos InputStream resuelto).
     */
    InputStream downloadFile(String blobUri);

    /**
     * Genera una URL temporal segura (SAS - Shared Access Signature) o equivalente
     * para descargas delegadas al frontend sin pasar el binario por el backend.
     * 
     * @param blobUri URL absoluta original registrada
     * @return URL con token embebido
     */
    String generateSecureUrl(String blobUri);
}
