package com.ibpms.poc.application.dto;

import java.util.Map;

public record NlpPromptRequestDto(
        String processId,
        String naturalLanguageText,
        Map<String, Object> promptSettings
) {}
