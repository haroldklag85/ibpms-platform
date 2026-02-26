package com.ibpms.poc.infrastructure.storage;

import com.ibpms.poc.application.port.out.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adaptador Driven — Almacenamiento Local (PoC V1).
 * Implementa StoragePort usando el sistema de archivos.
 * En V2 se reemplaza con AzureBlobStorageAdapter sin cambios en el dominio.
 */
@Component
public class LocalStorageAdapter implements StoragePort {

    private final Path storageRoot;

    public LocalStorageAdapter(@Value("${app.storage.path:./uploads}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear el directorio de almacenamiento: " + storagePath, e);
        }
    }

    @Override
    public String almacenar(String nombre, byte[] contenido) {
        Path destino = storageRoot.resolve(nombre);
        try {
            Files.write(destino, contenido);
            return destino.toUri().toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al almacenar el archivo: " + nombre, e);
        }
    }

    @Override
    public byte[] recuperar(String uri) {
        try {
            return Files.readAllBytes(Paths.get(java.net.URI.create(uri)));
        } catch (IOException e) {
            throw new UncheckedIOException("Error al recuperar el archivo: " + uri, e);
        }
    }

    @Override
    public void eliminar(String uri) {
        try {
            Files.deleteIfExists(Paths.get(java.net.URI.create(uri)));
        } catch (IOException e) {
            throw new UncheckedIOException("Error al eliminar el archivo: " + uri, e);
        }
    }
}
