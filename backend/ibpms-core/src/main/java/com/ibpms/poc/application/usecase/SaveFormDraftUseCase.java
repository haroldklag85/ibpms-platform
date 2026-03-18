package com.ibpms.poc.application.usecase;

import java.util.Map;
import java.util.UUID;

public interface SaveFormDraftUseCase {
    void execute(UUID formId, Map<String, Object> payload);
}
