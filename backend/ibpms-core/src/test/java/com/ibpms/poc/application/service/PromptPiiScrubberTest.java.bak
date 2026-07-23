package com.ibpms.poc.application.service;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PromptPiiScrubberTest {

    private final PromptPiiScrubber scrubber = new PromptPiiScrubber();

    @Test
    void testScrubPiiVariables() {
        String prompt = "Crear tabla con input variables: cliente_dni, nombre_completo, y edad. Output: riesgo_crediticio";
        
        PromptPiiScrubber.ScrubResult result = scrubber.scrub(prompt);
        
        // Verifica que se reemplazan variables PII comunes
        assertTrue(result.getScrubbedPrompt().contains("var_1"));
        assertTrue(result.getScrubbedPrompt().contains("var_2"));
        assertFalse(result.getScrubbedPrompt().contains("cliente_dni"));
        assertFalse(result.getScrubbedPrompt().contains("nombre_completo"));
        
        // El output o variables no-PII no deberían reemplazarse, o sí, si escrapeamos todo.
        // Pero la instrucción dice: seudonimización aplica a NOMBRES DE VARIABLES del diccionario.
        Map<String, String> dictionary = result.getReverseDictionary();
        assertTrue(dictionary.containsKey("var_1"));
        assertTrue(dictionary.containsKey("var_2"));
        assertTrue(dictionary.containsValue("cliente_dni"));
        assertTrue(dictionary.containsValue("nombre_completo"));
    }

    @Test
    void testReverseScrubbing() {
        String mockXml = "<input id=\"var_1\"><output id=\"var_2\">";
        Map<String, String> dict = Map.of("var_1", "cliente_dni", "var_2", "nombre_completo");
        
        String reversed = scrubber.restore(mockXml, dict);
        
        assertTrue(reversed.contains("cliente_dni"));
        assertTrue(reversed.contains("nombre_completo"));
        assertFalse(reversed.contains("var_1"));
    }
}
