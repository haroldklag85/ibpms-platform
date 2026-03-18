package com.ibpms.poc.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface DocumentStoragePort {
    String saveDocument(UUID documentId, MultipartFile file);
}
