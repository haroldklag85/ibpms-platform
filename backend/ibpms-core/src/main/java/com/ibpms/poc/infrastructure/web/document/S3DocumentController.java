package com.ibpms.poc.infrastructure.web.document;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.ibpms.poc.application.service.bff.S3DocumentTempService;
import java.util.Map;

/**
 * Misión Cero: Scaffold S3 Upload-Temp para US-029.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class S3DocumentController {

    private final S3DocumentTempService s3Service;

    @Autowired
    public S3DocumentController(S3DocumentTempService s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload-temp")
    public ResponseEntity<?> uploadTempDocument() {
        String uuid = s3Service.uploadTemporaryDocument();
        return ResponseEntity.ok(Map.of(
            "message", "Upload MOCK exitoso hacia Bóveda SGDEA",
            "temp_id", uuid
        ));
    }
}
