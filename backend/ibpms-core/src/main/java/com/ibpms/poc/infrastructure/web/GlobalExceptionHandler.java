package com.ibpms.poc.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Manejador Global de Excepciones — RFC 7807 Problem Details.
 * Todas las respuestas de error siguen el mismo formato JSON estándar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 — Error de validación de campos (@Valid) */
    @ApiResponse(responseCode = "400", description = "Error de validación de campos en el Payload", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/validation-error")));
        problem.setTitle("Error de validación");
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        problem.setDetail(detail);
        return problem;
    }

    /** 400 — Petición malformada (XML Inválido, argumentos faltantes) */
    @ApiResponse(responseCode = "400", description = "Petición estructuralmente malformada", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/bad-request")));
        problem.setTitle("Petición inválida");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 404 — Recurso no encontrado */
    @ApiResponse(responseCode = "404", description = "El recurso solicitado no fue encontrado", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ProblemDetail handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/not-found")));
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 409 — Conflicto de idempotencia */
    @ApiResponse(responseCode = "409", description = "Conflicto por violación de reglas de Idempotencia o Estado Híbrido", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleConflict(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/conflict")));
        problem.setTitle("Conflicto de estado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 409 — Conflicto de Tarea ya Asignada */
    @ApiResponse(responseCode = "409", description = "La tarea BPMN ya fue reclamada por otro usuario", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(com.ibpms.poc.domain.exception.TaskAlreadyClaimedException.class)
    public ProblemDetail handleTaskAlreadyClaimed(com.ibpms.poc.domain.exception.TaskAlreadyClaimedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/task-claimed")));
        problem.setTitle("Tarea ya reclamada");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 400 — Límite de Rebotes Superado */
    @ApiResponse(responseCode = "400", description = "Límite cíclico de reasignaciones superado en BPMN (Ping-Pong)", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(com.ibpms.poc.domain.exception.TaskReassignmentLimitException.class)
    public ProblemDetail handleReassignmentLimit(com.ibpms.poc.domain.exception.TaskReassignmentLimitException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/task-reassignment-limit")));
        problem.setTitle("Límite de Reasignaciones Excedido");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 500 — Error interno genérico */
    @ApiResponse(responseCode = "500", description = "Error interno no controlado en el Engine", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/internal-error")));
        problem.setTitle("Error interno del servidor");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
