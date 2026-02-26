package com.ibpms.poc.application.port.out;

/**
 * Puerto de Salida: Almacenamiento de Documentos.
 * En V1 se implementa con almacenamiento local (disco).
 * En V2 se reemplaza con Azure Blob Storage sin cambios en el dominio.
 */
public interface StoragePort {

    /**
     * Almacena un documento y devuelve la URI de acceso.
     * 
     * @param nombre    nombre del archivo (incluyendo extensión)
     * @param contenido bytes del archivo
     * @return URI de acceso al archivo almacenado
     */
    String almacenar(String nombre, byte[] contenido);

    /**
     * Recupera el contenido de un documento por su URI.
     * 
     * @param uri URI devuelta por {@link #almacenar(String, byte[])}
     * @return bytes del archivo
     */
    byte[] recuperar(String uri);

    /**
     * Elimina un documento por su URI.
     * 
     * @param uri URI del archivo a eliminar
     */
    void eliminar(String uri);
}
