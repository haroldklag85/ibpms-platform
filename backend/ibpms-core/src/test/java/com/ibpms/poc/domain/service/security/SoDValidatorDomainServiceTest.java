package com.ibpms.poc.domain.service.security;

import com.ibpms.poc.domain.exception.SoDViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoDValidatorDomainServiceTest {

    private final SoDValidatorDomainService service = new SoDValidatorDomainService();

    @Test
    void testValidate_DifferentUsers_Success() {
        assertDoesNotThrow(() -> service.validate("user1", "user2"));
    }

    @Test
    void testValidate_SameUser_ThrowsSoDViolationException() {
        SoDViolationException exception = assertThrows(SoDViolationException.class, () -> {
            service.validate("admin", "admin");
        });
        assertTrue(exception.getMessage().contains("admin"));
        assertTrue(exception.getMessage().contains("Violación de Segregación de Funciones (SoD)"));
    }

    @Test
    void testValidate_SameUserDifferentCase_ThrowsSoDViolationException() {
        assertThrows(SoDViolationException.class, () -> {
            service.validate("ADMIN", "admin");
        });
    }

    @Test
    void testValidate_NullCreator_Success() {
        assertDoesNotThrow(() -> service.validate(null, "admin"));
    }

    @Test
    void testValidate_NullApprover_Success() {
        assertDoesNotThrow(() -> service.validate("admin", null));
    }
}
