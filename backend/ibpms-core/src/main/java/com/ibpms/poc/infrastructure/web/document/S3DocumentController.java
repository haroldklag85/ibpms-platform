package com.ibpms.poc.infrastructure.web.document;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ibpms.poc.application.service.bff.S3DocumentTempService;
import com.ibpms.poc.infrastructure.jpa.entity.TempDocumentEntity;
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
    public ResponseEntity<?> uploadTempDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") String taskId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = "anonymous";
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            userId = ((Jwt) auth.getPrincipal()).getClaimAsString("preferred_username");
        }

        TempDocumentEntity doc = s3Service.uploadTemporaryDocument(file, taskId, userId);
        return ResponseEntity.ok(Map.of(
                "temp_id", doc.getId().toString(),
                "filename", doc.getFilename(),
                "size", doc.getSizeBytes()));
    }
}
