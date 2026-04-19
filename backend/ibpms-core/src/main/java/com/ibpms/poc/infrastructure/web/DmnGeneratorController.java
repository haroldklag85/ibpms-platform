package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.DmnXmlResponseDto;
import com.ibpms.poc.application.dto.NlpPromptRequestDto;
import com.ibpms.poc.application.port.out.AiDmnGeneratorPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint responsable de canalizar las peticiones de generación de tablas de decisión 
 * DMN mediante IA Generativa interactiva.
 */
@RestController
@RequestMapping("/api/v1/dmn")
public class DmnGeneratorController {

    private final AiDmnGeneratorPort aiDmnGeneratorPort;

    public DmnGeneratorController(AiDmnGeneratorPort aiDmnGeneratorPort) {
        this.aiDmnGeneratorPort = aiDmnGeneratorPort;
    }

    /**
     * DTO de entrada para la generación DMN con restricciones JSR-380 rigurosas (US-007 CA-19 y CA-20).
     */
    public record GenerateDmnRequest(
            @NotBlank(message = "El prompt de entrada corporativo no puede estar vacío.")
            @Size(max = 2000, message = "El prompt excede el límite máximo de memoria contextual (2000 caracteres).")
            String prompt
    ) {}

    /**
     * US-007 CA-24: SLA timeout 15s con CompletableFuture.orTimeout().
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.concurrent.CompletableFuture<ResponseEntity<DmnXmlResponseDto>> generateDmn(@Valid @RequestBody GenerateDmnRequest request) {
        NlpPromptRequestDto portRequest = new NlpPromptRequestDto(request.prompt());
        
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            DmnXmlResponseDto response = aiDmnGeneratorPort.generateDmnFromPrompt(portRequest);
            return ResponseEntity.ok(response);
        }).orTimeout(15, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * US-007 CA-23: Rate Limiting Simulador.
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "dmnSimulator", fallbackMethod = "simulateRateLimitFallback")
    public ResponseEntity<String> simulateDmn(@RequestBody java.util.Map<String, Object> variables) {
        return ResponseEntity.ok("Simulación Exitosa"); // Stub Iteration 4
    }

    public ResponseEntity<String> simulateRateLimitFallback(java.util.Map<String, Object> variables, io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body("Rate limit excedido (10 req/min). Por favor espere.");
    }
}
