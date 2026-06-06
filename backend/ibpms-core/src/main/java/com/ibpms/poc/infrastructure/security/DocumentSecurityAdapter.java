package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.domain.port.DocumentSecurityPort;
import com.ibpms.poc.infrastructure.jpa.entity.TempDocumentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.TempDocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentSecurityAdapter implements DocumentSecurityPort {

    private final TempDocumentRepository tempDocumentRepository;

    public DocumentSecurityAdapter(TempDocumentRepository tempDocumentRepository) {
        this.tempDocumentRepository = tempDocumentRepository;
    }

    @Override
    public void confirmOwnershipAndMarkConfirmed(UUID documentId, String taskId, String userId) {
        Optional<TempDocumentEntity> docOpt = tempDocumentRepository.findById(documentId);
        if (docOpt.isPresent()) {
            TempDocumentEntity doc = docOpt.get();
            if (!doc.getTaskId().equals(taskId) || !doc.getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anti-IDOR: No tiene acceso al archivo temporal " + documentId);
            }
            doc.setStatus("CONFIRMED");
            tempDocumentRepository.save(doc);
        }
    }
}
