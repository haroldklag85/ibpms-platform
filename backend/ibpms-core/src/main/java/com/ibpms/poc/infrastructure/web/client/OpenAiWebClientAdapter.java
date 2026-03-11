package com.ibpms.poc.infrastructure.web.client;

import com.ibpms.dmn.NlpAgentPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Adaptador Driven para comunicarse mediante HTTP con OpenAI (GPT-4) o un
 * proxy LLM corporativo.
 */
@Component
public class OpenAiWebClientAdapter implements NlpAgentPort {

    private final WebClient webClient;
    private final String openAiBaseUrl;
    private final String openAiModel;
    private final String openAiApiKey;

    public OpenAiWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String openAiBaseUrl,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String openAiModel,
            @Value("${app.ai.openai.api-key:dummy-key}") String openAiApiKey) {
        this.openAiBaseUrl = openAiBaseUrl;
        this.openAiModel = openAiModel;
        this.openAiApiKey = openAiApiKey;

        this.webClient = webClientBuilder
                .baseUrl(java.util.Objects.requireNonNull(this.openAiBaseUrl))
                .defaultHeader("Authorization", java.util.Objects.requireNonNull("Bearer " + this.openAiApiKey))
                .build();
    }

    @Override
    public String generateDmnXml(String naturalLanguagePrompt) {
        String systemPrompt = "Eres un ingeniero de hiperautomatización experto en Camunda Platform 7. " +
                "Debes leer la regla en lenguaje humano e iterar sobre ella para construir una Tabla de Decisión (DMN 1.3) que la resuelva. "
                +
                "REGLAS OBLIGATORIAS:\n" +
                "1. Asegúrate de declarar xmlns=\"https://www.omg.org/spec/DMN/20191111/MODEL/\" y namespace=\"http://camunda.org/schema/1.0/dmn\".\n"
                +
                "2. NO envuelvas NADA en Markdown de código (```xml ... ```), NUNCA regreses markdown.\n" +
                "3. Regresa el String crudo comenzando estrictamente con <?xml version=\"1.0\" encoding=\"UTF-8\"?>.\n"
                +
                "4. No añadas descripciones, introducciones o resúmenes.";

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", naturalLanguagePrompt)),
                "temperature", 0.1);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(java.util.Objects.requireNonNull(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())))
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Error contactando a OpenAI: " + e.getMessage());
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions id=\"ErrorFallback\" name=\"ErrorFallbackDMN\"></definitions>";
    }

    @Override
    public String generateSuggestedReply(String emailBody) {
        String systemPrompt = "Eres un agente de servicio al cliente. Lee el siguiente email y redacta un borrador de respuesta profesional en español, corto y conciso.";
        return callOpenAi(systemPrompt, emailBody, "Error: No se pudo generar una respuesta sugerida.");
    }

    @Override
    public String extractLegalSla(String emailBody) {
        String systemPrompt = "Eres un asistente legal. Analiza el siguiente texto y extrae EXCLUSIVAMENTE el tiempo límite o SLA mencionado (ej. '10 días hábiles', '48 horas'). Si no encuentras ninguno, responde exactamante 'N/A'. NO añadas ninguna otra palabra.";
        return callOpenAi(systemPrompt, emailBody, "N/A");
    }

    private String callOpenAi(String systemPrompt, String userMessage, String fallbackValue) {
        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)),
                "temperature", 0.3);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(java.util.Objects.requireNonNull(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())))
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Error contactando a OpenAI (Helper): " + e.getMessage());
        }

        return fallbackValue;
    }

    @Override
    public List<Double> generateEmbeddings(String text) {
        Map<String, Object> requestBody = Map.of(
                "model", "text-embedding-3-small", // Ideal para RAG y dimensionalidad baja
                "input", text);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(java.util.Objects.requireNonNull(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())))
                    .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                    return embedding;
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando Embeddings desde OpenAI: " + e.getMessage());
        }

        return List.of(); // Retorna vacío si falla, o se podría manejar con Excepción
    }

    @Override
    public String analyzeBpmnSemantics(String bpmnXml, boolean iso9001Strict) {
        String systemPrompt = "Eres un Auditor de Calidad " + (iso9001Strict ? "estricto (ISO 9001) " : "ágil ") +
                "y un Arquitecto de Software iBPMS experto en Camunda/BPMN 2.0. " +
                "Analiza el siguiente XML BPMN buscando:\n" +
                "1. Tareas humanas sin SLAs (no compliance ISO 9001).\n" +
                "2. ServiceTasks sin ErrorBoundary o retry (riesgo de Sagas rotas).\n" +
                "3. Tareas manuales repetitivas u oportunidades de automatización con RPA o DMN.\n\n" +
                "Tu respuesta DEBE ser EXCLUSIVAMENTE un JSON válido que cumpla con este schema exacto, emitiendo listas vacias [] si no hay hallazgos:\n"
                +
                "{\n" +
                "  \"criticalIsoViolations\": [\n" +
                "    { \"elementId\": \"taskId\", \"elementName\": \"name\", \"issue\": \"issue\", \"recommendation\": \"rec\" }\n"
                +
                "  ],\n" +
                "  \"structuralAntiPatterns\": [],\n" +
                "  \"automationOpportunities\": [],\n" +
                "  \"internalScore1To100\": 85\n" +
                "}";

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", bpmnXml)),
                "temperature", 0.1);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(java.util.Objects.requireNonNull(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Map.of("error", e.getMessage())))
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Error en Auditoría BPMN OpenAI: " + e.getMessage());
        }

        // Estructura JSON de fallback (Circuito roto)
        return "{\"criticalIsoViolations\":[],\"structuralAntiPatterns\":[{\"elementId\":\"SYSTEM_ERROR\",\"elementName\":\"AI Gateway\",\"issue\":\"Falló la conexión al LLM API\",\"recommendation\":\"Verifica los logs o la API Key\"}],\"automationOpportunities\":[],\"internalScore1To100\":0}";
    }
}
