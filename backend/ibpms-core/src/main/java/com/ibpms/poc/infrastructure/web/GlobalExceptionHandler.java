package com.ibpms.poc.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Manejador Global de Excepciones — RFC 7807 Problem Details.
 * Todas las respuestas de error siguen el mismo formato JSON estándar.
 */
@RestControllerAdvice
@Slf4j
@Traceability(US = "US-000", CA = {"CA-01", "CA-02", "CA-03", "CA-04", "CA-37"})
public class GlobalExceptionHandler {

    /** 400 — Error de validación de campos (@Valid) */
    // @Traceability: US-000 - CA-2
    @ApiResponse(responseCode = "400", description = "Error de validación de campos en el Payload", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ibpms.com/errors/validation-error"));
        problem.setTitle("Error de validación");
        problem.setDetail("Se hallaron errores de validación en la solicitud.");
        
        // Mapeo estructurado {field, issue} para que Frontend dibuje inputs rojos
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "issue", fe.getDefaultMessage()))
                .collect(Collectors.toList());
                
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    /** 400 — Error de validación de base de datos o path/query params (ConstraintViolation) US-029 CA-2 */
    @ApiResponse(responseCode = "400", description = "Error de validación de Constraints", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationError(jakarta.validation.ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ibpms.com/errors/constraint-violation"));
        problem.setTitle("Error de validación de Constraints");
        problem.setDetail("Se hallaron violaciones a las restricciones predefinidas.");
        
        List<Map<String, Object>> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", cv.getPropertyPath().toString(), 
                        "message", cv.getMessage(),
                        "rejectedValue", cv.getInvalidValue() != null ? cv.getInvalidValue() : "null"
                ))
                .collect(Collectors.toList());
                
        problem.setProperty("errors", fieldErrors);
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

    // CA-3: Bloqueo de Concurrencia Optimista (409)
    // @Traceability: US-000 - CA-3
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrency(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create("https://ibpms.com/errors/optimistic-lock"));
        problem.setTitle("Conflicto de Múltiples Operadores");
        problem.setDetail("Datos oxidados. El registro fue modificado por otro operador recientemente. Por favor, refresque y vuelva a intentar.");
        return problem;
    }

    /** 403 — Violación de Propiedad de Tarea (US-029 Zero Trust) */
    @ApiResponse(responseCode = "403", description = "Acceso denegado: Violación de propiedad de tarea", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(com.ibpms.poc.domain.exception.TaskOwnershipViolationException.class)
    public ProblemDetail handleTaskOwnershipViolation(com.ibpms.poc.domain.exception.TaskOwnershipViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/forbidden")));
        problem.setTitle("Acceso Denegado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /** 403 — Acceso Denegado (Fallos genéricos de @PreAuthorize) */
    @ApiResponse(responseCode = "403", description = "Acceso denegado: Privilegios insuficientes", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        ex.printStackTrace(); // TEMP DIAGNOSTIC
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/forbidden")));
        problem.setTitle("Acceso Denegado");
        problem.setDetail("No tienes suficientes privilegios para acceder a este recurso.");
        return problem;
    }

    /** Maneja ResponseStatusException devolviendo el status y razón específicos */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode());
        problem.setType(URI.create("https://ibpms.com/errors/status"));
        problem.setTitle(ex.getReason() != null ? ex.getReason() : "Error " + ex.getStatusCode().value());
        problem.setDetail(ex.getReason());
        return problem;
    }

    /** 500 — Error interno genérico (CA-37) */
    // @Traceability: US-000 - CA-1
    // @Traceability: US-000 - CA-4
    @ApiResponse(responseCode = "500", description = "Error interno - Blindado", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("💥 ERROR CRITICO DEL SISTEMA ENVIADO A ELK: ", ex); // Delegación a Logback/ELK
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Error interno del servidor");
        problem.setDetail("Error interno del servidor"); // Enmascaramiento total de PII, tal como exige CA-37
        return problem;
    }
    /** 504 — Tiempo de Espera Agotado (SLA CA-24, CA-31) */
    @ApiResponse(responseCode = "504", description = "Gateway Timeout", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProblemDetail.class)))
    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ProblemDetail handleTimeoutError(java.util.concurrent.TimeoutException ex) {
        log.warn("⏳ SLA Timeout interceptado: ", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.GATEWAY_TIMEOUT);
        problem.setType(URI.create("https://ibpms.com/errors/gateway-timeout"));
        problem.setTitle("Tiempo de respuesta excedido");
        problem.setDetail("La operación no pudo completarse dentro del umbral de tiempo (SLA).");
        return problem;
    }
}
