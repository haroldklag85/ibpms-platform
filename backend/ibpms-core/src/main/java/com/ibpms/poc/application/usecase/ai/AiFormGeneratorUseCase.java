package com.ibpms.poc.application.usecase.ai;

import org.springframework.stereotype.Service;

/**
 * CA-73: Escáner Mágico (AI Prompt-to-Form).
 * Orquesta la llamada al LLM (Ej. gpt-4-turbo o claude-3-opus) forzando
 * la respuesta estructurada en JSON (`response_format: { type: "json_object" }`).
 */
@Service
public class AiFormGeneratorUseCase {

    // En un entorno real se inyecta Spring AI (ChatClient) o LangChain4j
    // private final ChatClient chatClient;

    public String generateFormJson(String prompt) {
        // System Prompt Oculto para blindar la estructura y prevenir "Markdown Charlatán"
        @SuppressWarnings("unused")
        String systemPrompt = "Eres un arquitecto UI experto en Vue 3 y Zod. " +
            "GENERARÁS EXCLUSIVAMENTE UN OBJETO JSON VÁLIDO. PROHIBIDO ESCRIBIR TEXTO LIBRE O MARKDOWN. " +
            "El esquema debe contener un array 'components' donde cada uno tiene: " +
            "'id' (snake_case), 'type' (text, number, select, etc), 'label', y 'zod' (string con la regla z.algo()). " +
            "Basate en la siguiente solicitud: ";

        // Simulador de Inferencia LLM (Mock BDD) garantizando el contrato
        // return chatClient.prompt(systemPrompt + prompt).call().content();
        
        return "{\n" +
               "  \"components\": [\n" +
               "    {\n" +
               "      \"id\": \"cliente_nombre\",\n" +
               "      \"type\": \"text\",\n" +
               "      \"label\": \"Nombre Completo\",\n" +
               "      \"required\": true,\n" +
               "      \"zod\": \"z.string().min(3)\"\n" +
               "    },\n" +
               "    {\n" +
               "      \"id\": \"monto_credito\",\n" +
               "      \"type\": \"number\",\n" +
               "      \"label\": \"Monto Solicitado\",\n" +
               "      \"required\": true,\n" +
               "      \"zod\": \"z.number().positive()\"\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
}
