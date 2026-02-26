package com.ibpms.poc.infrastructure.web.client;

import com.ibpms.poc.application.port.out.NlpAgentPort;
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
                .baseUrl(this.openAiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + this.openAiApiKey)
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
                    .bodyValue(requestBody)
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
}
