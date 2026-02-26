package com.ibpms.poc.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Manejador Global de Excepciones — RFC 7807 Problem Details.
 * Todas las respuestas de error siguen el mismo formato JSON estándar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 — Error de validación de campos (@Valid) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ibpms.com/errors/validation-error"));
        problem.setTitle("Error de validación");
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        problem.setDetail(detail);
        return problem;
    }

    /** 400 — Petición malformada (XML Inválido, argumentos faltantes) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ibpms.com/errors/bad-request"));
        problem.setTitle("Petición inválida");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 404 — Recurso no encontrado */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ProblemDetail handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://ibpms.com/errors/not-found"));
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 409 — Conflicto de idempotencia */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleConflict(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://ibpms.com/errors/conflict"));
        problem.setTitle("Conflicto de estado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 500 — Error interno genérico */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://ibpms.com/errors/internal-error"));
        problem.setTitle("Error interno del servidor");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
