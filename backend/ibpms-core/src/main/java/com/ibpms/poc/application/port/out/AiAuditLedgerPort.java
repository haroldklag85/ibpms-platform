package com.ibpms.poc.application.port.out;

/**
 * Capa Anti-Corrupción: Ledger inmutable de auditoría (eDiscovery).
 * Garantiza la trazabilidad de las acciones de la IA vs Humanos.
 */
public interface AiAuditLedgerPort {

    /**
     * Registra la sugerencia inicial propuesta por el LLM.
     * 
     * @param businessKey   ID del proceso/correo.
     * @param aiModel       Versión del modelo (ej. gpt-4o-mini).
     * @param systemPrompt  Instrucciones dadas al modelo.
     * @param suggestedText Borrador generado.
     */
    void logAiSuggestion(String businessKey, String aiModel, String systemPrompt, String suggestedText);

    /**
     * Registra la decisión final del operador humano tras revisar el borrador de la
     * IA.
     * 
     * @param businessKey     ID del proceso/correo.
     * @param operatorId      Email o ID del gestor humano.
     * @param finalSentText   El texto que finalmente se emitió al cliente.
     * @param similarityScore Qué tanto conservó el humano la idea de la IA
     *                        (distancia Levenshtein / score RAG).
     */
    void logHumanResolution(String businessKey, String operatorId, String finalSentText, double similarityScore);
}
