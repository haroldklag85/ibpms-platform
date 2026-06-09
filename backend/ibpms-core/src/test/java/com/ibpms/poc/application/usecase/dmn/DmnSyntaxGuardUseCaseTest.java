package com.ibpms.poc.application.usecase.dmn;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class DmnSyntaxGuardUseCaseTest {

    private final DmnSyntaxGuardUseCase guard = new DmnSyntaxGuardUseCase();

    @Test
    void testTruncatePrompt() {
        String prompt = "A".repeat(5000);
        String result = guard.validateAndTruncatePrompt(prompt);
        assertEquals(4096, result.length());
    }

    @Test
    void testDateMathRejection() {
        String xml = "<text>date and time(\"2023-01-01T00:00:00\")</text>";
        assertThrows(ResponseStatusException.class, () -> guard.validateAiOutputXml(xml));
        
        String xml2 = "<text>duration(\"P1Y\")</text>";
        assertThrows(ResponseStatusException.class, () -> guard.validateAiOutputXml(xml2));
        
        String xml3 = "<text>now()</text>";
        assertThrows(ResponseStatusException.class, () -> guard.validateAiOutputXml(xml3));
    }

    @Test
    void testFeelLowercase() {
        String xml = "<text>\"APROBADO\"</text>";
        String result = guard.applyFeelLowercase(xml);
        assertTrue(result.contains("\"aprobado\""));
    }

    @Test
    void testOverlapCheck() {
        String xml = "<text>[10..20]</text><text>[15..30]</text>";
        assertThrows(ResponseStatusException.class, () -> guard.validateAiOutputXml(xml));
        
        String xmlOk = "<text>[10..20]</text><text>[21..30]</text>";
        assertDoesNotThrow(() -> guard.validateAiOutputXml(xmlOk));
    }
}
