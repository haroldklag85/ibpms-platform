package com.ibpms.poc.application.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromptNormalizerTest {

    private final PromptNormalizer normalizer = new PromptNormalizer();

    @Test
    void testNormalizePrompt() {
        String prompt1 = "Aprobar si MONTO < 1000.";
        String prompt2 = "  aprobar si monto < 1000  ";
        String prompt3 = "Aprobar, si monto < 1000";

        String expected = "aprobar si monto < 1000";

        assertEquals(expected, normalizer.normalize(prompt1));
        assertEquals(expected, normalizer.normalize(prompt2));
        assertEquals(expected, normalizer.normalize(prompt3));
    }
}
