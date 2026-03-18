package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.out.DocumentStoragePort;
import com.ibpms.poc.application.port.out.FormDraftPort;
import com.ibpms.poc.application.usecase.SaveFormDraftUseCase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
public class FormStorageService implements SaveFormDraftUseCase {
    
    private final DocumentStoragePort documentStoragePort;
    private final FormDraftPort formDraftPort;

    public FormStorageService(DocumentStoragePort documentStoragePort, FormDraftPort formDraftPort) {
        this.documentStoragePort = documentStoragePort;
        this.formDraftPort = formDraftPort;
    }

    @Override
    public void execute(UUID formId, Map<String, Object> payload) {
        formDraftPort.saveDraft(formId, payload);
    }

    public Map<String, Object> getDraft(UUID formId) {
        return formDraftPort.getDraft(formId);
    }

    public String saveDocument(UUID documentId, MultipartFile file) {
        return documentStoragePort.saveDocument(documentId, file);
    }
}
