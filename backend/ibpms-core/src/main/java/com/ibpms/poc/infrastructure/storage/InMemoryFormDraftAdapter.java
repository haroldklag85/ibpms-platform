package com.ibpms.poc.infrastructure.storage;

import com.ibpms.poc.application.port.out.FormDraftPort;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryFormDraftAdapter implements FormDraftPort {
    
    // CA-24: Memoria volátil transaccional 
    private final Map<UUID, Map<String, Object>> draftStore = new ConcurrentHashMap<>();

    @Override
    public void saveDraft(UUID formId, Map<String, Object> draftData) {
        draftStore.put(formId, draftData);
    }

    @Override
    public Map<String, Object> getDraft(UUID formId) {
        return draftStore.get(formId);
    }
}
