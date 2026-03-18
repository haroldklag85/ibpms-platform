package com.ibpms.poc.infrastructure.storage;

import com.ibpms.poc.application.port.out.DocumentStoragePort;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class LocalDocumentStorageAdapter implements DocumentStoragePort {

    @Override
    public String saveDocument(UUID documentId, MultipartFile file) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, documentId.toString() + "_" + file.getOriginalFilename());
            file.transferTo(tempFile);
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error guardando el archivo temporal (CA-21)", e);
        }
    }
}
