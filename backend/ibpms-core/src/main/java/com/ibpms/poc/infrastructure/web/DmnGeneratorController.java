// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.DmnXmlResponseDto;
import com.ibpms.poc.application.dto.NlpPromptRequestDto;
import com.ibpms.poc.application.port.out.AiDmnGeneratorPort;
import com.ibpms.poc.application.service.dmn.DmnHitPolicyMutatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibpms.poc.application.util.SecurityContextUtils;
import com.ibpms.poc.crosscutting.annotations.Traceability;

/**
 * Endpoint responsable de canalizar las peticiones de generación de tablas de decisión 
 * DMN mediante IA Generativa interactiva.
 */
@RestController
@RequestMapping("/api/v1/dmn")
@Traceability(US = "US-007", CA = {"CA-19", "CA-20", "CA-23", "CA-24"})
public class DmnGeneratorController {

    private final AiDmnGeneratorPort aiDmnGeneratorPort;
    private final DmnHitPolicyMutatorService dmnHitPolicyMutatorService;

    public DmnGeneratorController(AiDmnGeneratorPort aiDmnGeneratorPort, DmnHitPolicyMutatorService dmnHitPolicyMutatorService) {
        this.aiDmnGeneratorPort = aiDmnGeneratorPort;
        this.dmnHitPolicyMutatorService = dmnHitPolicyMutatorService;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public java.util.concurrent.CompletableFuture<ResponseEntity<DmnXmlResponseDto>> generateDmn(@Valid @RequestBody GenerateDmnRequest request) {
        String tenantId = SecurityContextUtils.getTenantId();
        NlpPromptRequestDto portRequest = new NlpPromptRequestDto(request.prompt(), tenantId, java.util.Collections.emptyMap());
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            DmnXmlResponseDto response = aiDmnGeneratorPort.generateDmnFromPrompt(portRequest);
            return ResponseEntity.ok(response);
        }).orTimeout(15, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * US-007 CA-23: Rate Limiting Simulador.
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "dmnSimulator", fallbackMethod = "simulateRateLimitFallback")
    public ResponseEntity<String> simulateDmn(@RequestBody java.util.Map<String, Object> variables) {
        return ResponseEntity.ok("Simulación Exitosa"); // Stub Iteration 4
    }

    public ResponseEntity<String> simulateRateLimitFallback(java.util.Map<String, Object> variables, io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body("Rate limit excedido (10 req/min). Por favor espere.");
    }

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.APPLICATION_XML_VALUE, produces = org.springframework.http.MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Object> uploadDmn(@RequestBody String xmlContent) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<(?:[a-zA-Z0-9_-]+:)?rule\\b");
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);
        int ruleCount = 0;
        while (matcher.find()) {
            ruleCount++;
        }

        if (ruleCount > 50) {
            java.util.Map<String, String> errorResponse = java.util.Map.of(
                "error", "DMN_RULE_LIMIT_EXCEEDED",
                "message", "El número de reglas de negocio no puede superar el límite estricto de 50. (Detectadas: " + ruleCount + ")"
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        String mutatedXml = dmnHitPolicyMutatorService.enforceMathGuardrails(xmlContent);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                .body(mutatedXml);
    }
}
