package com.ibpms.poc.application.port.out;

import java.util.Map;
import java.util.UUID;

public interface FormDraftPort {
    void saveDraft(UUID formId, Map<String, Object> draftData);
    Map<String, Object> getDraft(UUID formId);
}
