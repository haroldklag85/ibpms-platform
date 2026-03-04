package com.ibpms.poc.application.service.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DlpInterceptorTest {

    private final DlpInterceptor dlpInterceptor = new DlpInterceptor();

    @Test
    @DisplayName("Debe censurar totalmente los SSN norteamericanos con [REDACTED_PII]")
    void redactSensibleData_SSN_IsMasked() {
        String payload = "El usuario John Doe tiene el SSN 123-45-6789 en su registro.";
        String result = dlpInterceptor.redactSensibleData(payload);

        assertEquals("El usuario John Doe tiene el SSN [REDACTED_PII] en su registro.", result);
    }

    @Test
    @DisplayName("Debe censurar tarjetas de crédito de 16 dígitos con [REDACTED_PII]")
    void redactSensibleData_CreditCard_IsMasked() {
        String payload = "Pagar con la tarjeta 4532 1234 5678 9012 hoy.";
        String result = dlpInterceptor.redactSensibleData(payload);

        assertEquals("Pagar con la tarjeta [REDACTED_PII] hoy.", result);
    }

    @Test
    @DisplayName("Debe censurar DNIs y Cédulas (8 a 10 dígitos) con [REDACTED_PII]")
    void redactSensibleData_Dni_IsMasked() {
        String payload = "La cédula del ciudadano es 1020304050 y su DNI español es 12345678.";
        String result = dlpInterceptor.redactSensibleData(payload);

        assertEquals("La cédula del ciudadano es [REDACTED_PII] y su DNI español es [REDACTED_PII].", result);
    }
}
