package com.ibpms.poc.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class GenerativeSreIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Supongamos que este es el cliente Feign o RestTemplate que despacha al LLM externo
    public interface LlmExternalClient {
        String generateResponse(String prompt);
    }

    // Mockeamos la clase en el Contexto de Spring para asercionar el número de invocaciones
    @MockBean
    private LlmExternalClient llmClient;

    @Test
    @DisplayName("US-007 CA-1: Ingeniería de Confiabilidad SRE - Aplica Rate Limiting Estricto en Endpoints IA (429 Too Many Requests)")
    void testRateLimiting_GenerativeEndpoint_Throws429AfterThreshold() throws Exception {
        String payload = "{\"prompt\": \"Analizar el contrato #999\"}";

        // Simulamos el Bucket4j / Resilience4j Rate Limiter programado a 5 req/seg.
        // Hacemos un bucle de 6 disparos ininterrumpidos haciéndonos pasar por el mismo UserID.
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/api/v1/ai/generate")
                    .header("X-Forwarded-For", "192.168.1.100") // Simulate single client
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
                   // Asumimos 200 OK (o análogo) en las primeras llamadas 
                   // (depende del test dummy endpoint, usamos is2xx para absorber 200/202)
                   .andExpect(status().is2xxSuccessful());
        }

        // 6ta petición paralela: Aserción SRE. El Gateway debe estrangular la conexión.
        mockMvc.perform(post("/api/v1/ai/generate")
                .header("X-Forwarded-For", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isTooManyRequests()); // HTTP 429
    }

    @Test
    @DisplayName("US-007 CA-2: Zero-Cost Caching - Identicos Prompts se devuelven desde RAM mitigando los cobros de API")
    void testZeroCostCache_IdenticalPromptsServeFromMemory() throws Exception {
        String identicalPrompt = "Resumir el documento A-101";
        String payload = "{\"prompt\": \"" + identicalPrompt + "\"}";
        String dummyLlmResponse = "{\"result\": \"Resumen Aprobado\"}";

        when(llmClient.generateResponse(anyString())).thenReturn(dummyLlmResponse);

        // Primera invocación (Miss Cache)
        mockMvc.perform(post("/api/v1/ai/generate")
                .header("X-Forwarded-For", "8.8.8.8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().is2xxSuccessful());

        // Segunda invocación Inmediata (Hit Cache Evitando el Facturador del LLM)
        mockMvc.perform(post("/api/v1/ai/generate")
                .header("X-Forwarded-For", "8.8.8.8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().is2xxSuccessful());

        // Aserción de Gobernanza Económica (SRE Cost Control):
        // El cliente de nube exterior físico (LLM) debió llamarse EXACTAMENTE 1 sola vez en el ciclo de vida
        verify(llmClient, times(1)).generateResponse(identicalPrompt);
    }
}
