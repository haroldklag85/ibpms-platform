package com.ibpms.poc.application.port.out;

import com.ibpms.poc.application.dto.DmnXmlResponseDto;
import com.ibpms.poc.application.dto.NlpPromptRequestDto;

public interface AiDmnGeneratorPort {
    /**
     * Genera una tabla de decisión en XML a partir de lenguaje natural (Vertex AI / Azure).
     */
    DmnXmlResponseDto generateDmnFromPrompt(NlpPromptRequestDto request);
}
