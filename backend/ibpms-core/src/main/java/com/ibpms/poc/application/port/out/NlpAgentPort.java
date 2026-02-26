package com.ibpms.poc.application.port.out;

public interface NlpAgentPort {
    /**
     * Llama al LLM (Ej. GPT-4, Claude) para traducir una regla natural a un XML DMN
     * 1.3
     */
    String generateDmnXml(String naturalLanguagePrompt);
}
