package com.ibpms.poc.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void test_Api_Returns400_WithValidationStructure() {
        // @Traceability: US-000 - CA-2
        FieldError fieldError1 = new FieldError("objectName", "email", "Formato de correo inválido");
        FieldError fieldError2 = new FieldError("objectName", "age", "Debe ser mayor a 18");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);

        // Act
        ProblemDetail problem = exceptionHandler.handleValidationError(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Error de validación", problem.getTitle());
        
        Object errorsObj = problem.getProperties().get("errors");
        assertNotNull(errorsObj, "El array de errores estructurados no debe ser nulo");
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> fieldErrors = (List<Map<String, String>>) errorsObj;
        assertEquals(2, fieldErrors.size());
        assertEquals("email", fieldErrors.get(0).get("field"));
        assertEquals("Formato de correo inválido", fieldErrors.get(0).get("issue"));
        assertEquals("age", fieldErrors.get(1).get("field"));
        assertEquals("Debe ser mayor a 18", fieldErrors.get(1).get("issue"));
    }

    @Test
    void test_Concurrent_Updates_Yields_409() {
        // @Traceability: US-000 - CA-3
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException(Object.class, 1L);

        // Act
        ProblemDetail problem = exceptionHandler.handleConcurrency(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Conflicto de Múltiples Operadores", problem.getTitle());
        assertTrue(problem.getDetail().contains("Datos oxidados"));
    }

    @Test
    void test_UncaughtExceptions_LogToELK_MaskStackTrace() {
        // @Traceability: US-000 - CA-1
        String secretErrorDetail = "NullPointerException at com.ibpms.poc.Service.doWork(Service.java:45)";
        Exception ex = new Exception(secretErrorDetail);

        // Act
        ProblemDetail problem = exceptionHandler.handleGeneral(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Error interno del servidor", problem.getTitle());
        
        // El detalle NO debe contener ninguna línea técnica ni rastros del mensaje original
        assertNotNull(problem.getDetail());
        assertFalse(problem.getDetail().contains(secretErrorDetail));
        assertFalse(problem.getDetail().contains("NullPointerException"));
        assertTrue(problem.getDetail().contains("Error interno del servidor"));
     }

    @Test
    void test_MethodNotSupported_Yields_405() {
        org.springframework.web.HttpRequestMethodNotSupportedException ex =
                new org.springframework.web.HttpRequestMethodNotSupportedException("POST", List.of("GET", "PUT"));

        // Act
        ProblemDetail problem = exceptionHandler.handleMethodNotSupported(ex);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), problem.getStatus());
        assertEquals("Método No Permitido", problem.getTitle());
        assertTrue(problem.getDetail().contains("POST"));
    }
}

