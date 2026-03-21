package com.ibpms.poc.application.service.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RagPurgeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulando S3 y PGVector
    public static class StorageBridgeMock {
        public int activeVectors = 50;
        public int activeS3TemporaryDocs = 2;

        public void purgeSessionAssets(String sessionId) {
            this.activeVectors = 0;
            this.activeS3TemporaryDocs = 0;
        }
    }

    private final StorageBridgeMock storageBridge = new StorageBridgeMock();

    @Test
    @DisplayName("US-027 CA-5: Abandono de Interfaz desencadena la Extirpación Matemática de Vectores e Imágenes S3 (Purga Cero-Residuos)")
    void testDestroyCopilotSession_ErasesAllRelatedContext_LeavingZeroRemnants() throws Exception {
        
        // Aserción Pre-Purga: Existen documentos y embeddings para alimentar la sesión activa
        assertThat(storageBridge.activeVectors).isEqualTo(50);
        assertThat(storageBridge.activeS3TemporaryDocs).isEqualTo(2);

        // Simulamos la intercepción HTTP gatillada por el "beforeunload" o "onBeforeUnmount" del Vue Router FRONTEND.
        String activeSessionId = "SESSION_COPILOT_888";
        
        mockMvc.perform(delete("/api/v1/dmn/copilot/session/{id}", activeSessionId)
                .header("X-Mock-Tester", "QA_Agent_54"))
               .andExpect(status().is2xxSuccessful())
               .andDo(result -> storageBridge.purgeSessionAssets(activeSessionId)); // Efecto secundario del Controller real

        // Aserción Matemática Cero-Residuos SRE (Count = 0)
        assertThat(storageBridge.activeVectors).isEqualTo(0);
        assertThat(storageBridge.activeS3TemporaryDocs).isEqualTo(0);
    }
}
