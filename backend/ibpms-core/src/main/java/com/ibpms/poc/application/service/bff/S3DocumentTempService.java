package com.ibpms.poc.application.service.bff;

import com.ibpms.poc.infrastructure.jpa.entity.TempDocumentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TempDocumentRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class S3DocumentTempService {

    private static final Logger log = LoggerFactory.getLogger(S3DocumentTempService.class);

    private final TempDocumentRepository tempDocumentRepository;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf","jpg","jpeg","png","gif","docx","xlsx","pptx","txt","csv");
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    public S3DocumentTempService(TempDocumentRepository tempDocumentRepository) {
        this.tempDocumentRepository = tempDocumentRepository;
    }

    public TempDocumentEntity uploadTemporaryDocument(MultipartFile file, String taskId, String userId) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 25MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have an extension");
        }
        
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extension not allowed");
        }

        String detectedMimeType;
        try {
            Tika tika = new Tika();
            detectedMimeType = tika.detect(file.getInputStream());
            // Magic bytes detection (si por ej detecta que es exe pero tiene .pdf, rechazar)
            // Aquí hay un chequeo simple: Tika nos da el mimeType real
            if (detectedMimeType.equals("application/x-msdownload") || detectedMimeType.equals("application/x-executable")) {
                 throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Executable files are not allowed");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error scanning file");
        }

        UUID tempUuid = UUID.randomUUID();
        
        try {
            Path dirPath = Paths.get("/tmp/ibpms-uploads", taskId);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(tempUuid.toString());
            file.transferTo(filePath.toFile());

            TempDocumentEntity doc = new TempDocumentEntity();
            doc.setId(tempUuid);
            doc.setTaskId(taskId);
            doc.setUserId(userId);
            doc.setFilename(originalFilename);
            doc.setSizeBytes(file.getSize());
            doc.setMimeType(detectedMimeType);
            doc.setStoragePath(filePath.toString());
            doc.setStatus("UPLOADED");
            doc.setUploadedAt(ZonedDateTime.now());
            
            return tempDocumentRepository.save(doc);
            
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file on disk");
        }
    }
}
