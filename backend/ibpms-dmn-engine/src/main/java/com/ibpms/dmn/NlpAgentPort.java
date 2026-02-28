package com.ibpms.dmn;

import java.util.List;

public interface NlpAgentPort {
    /**
     * Llama al LLM (Ej. GPT-4, Claude) para traducir una regla natural a un XML DMN
     * 1.3
     */
    String generateDmnXml(String naturalLanguagePrompt);

    /**
     * Llama al LLM para generar un borrador de respuesta basado en el mail
     */
    String generateSuggestedReply(String emailBody);

    /**
     * Extrae información de SLA (Ej. "10 días hábiles") del cuerpo del mail
     */
    String extractLegalSla(String emailBody);

    /**
     * Extrae el listado de vectores flotantes que representan el contexto (texto
     * dado).
     */
    List<Double> generateEmbeddings(String text);

    /**
     * Evalúa semánticamente la calidad técnica y funcional de un proceso BPMN.
     * 
     * @param bpmnXml       XML crudo del modelo.
     * @param iso9001Strict Habilita métricas de calidad y compliance más estrictas.
     * @return El JSON con el listado de antipatrones y sugerencias, estructurado.
     */
    String analyzeBpmnSemantics(String bpmnXml, boolean iso9001Strict);
}
