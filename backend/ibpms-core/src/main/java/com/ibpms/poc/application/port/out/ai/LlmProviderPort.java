package com.ibpms.poc.application.port.out.ai;

import java.util.List;

/**
 * Puerto de Salida (Secondary Port) - Hexagonal Architecture.
 * 
 * Interfaz agnóstica para comunicarse con cualquier Modelo de Lenguaje Grande
 * (LLM).
 * El motor iBPMS usará exclusivamente esta interfaz, siendo ciego al proveedor
 * real.
 * 
 * Implementaciones proyectadas:
 * V1: ApimVertexLlmAdapter (Consume APIs de Google/OpenAI a través de Azure
 * APIM).
 * V2 / Privacidad Extrema: LocalOllamaAdapter (Llama 3 / Mistral corriendo en
 * VMs propias).
 * 
 * El cambio de proveedor es cuestión de modificar una variable en el
 * application.yml
 * gracias a la inyección de dependencias de Spring.
 */
public interface LlmProviderPort {

    /**
     * Convierte un texto (ej. contenido de un expediente) en su representación
     * matemática.
     * 
     * @param text El texto a vectorizar.
     * @return El Embedding (lista de double).
     */
    List<Double> generateEmbedding(String text);

    /**
     * Motor RAG (Generación Aumentada por Recuperación).
     * Toma el prompt del usuario y los expedientes recuperados de PostgreSQL
     * para generar una respuesta en lenguaje natural.
     * 
     * @param userPrompt       La pregunta o petición original del usuario.
     * @param retrievedContext El bloque de texto de los documentos extraídos de la
     *                         Base de Datos.
     * @return Respuesta redactada por la IA.
     */
    String generateRagResponse(String userPrompt, String retrievedContext);

    /**
     * Inferencia pura o traducción estructurada (Ej. NLP a JSON DMN).
     * 
     * @param systemPrompt Instrucciones de sistema (System Message).
     * @param userMessage  Petición del usuario.
     * @return Salida del LLM.
     */
    String chat(String systemPrompt, String userMessage);
}
