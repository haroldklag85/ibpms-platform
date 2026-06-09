package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.SecurityAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.SecurityAuditLogRepository;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CA-04 US-038: Protocolo Break-Glass
 * Acceso de emergencia estático de último recurso.
 * 
 * @Traceability: Reactivación Break-Glass y Resolución de Colisión Semántica (T-24)
 * Resolución de Conflicto (Opción A - Híbrida):
 * Se reactiva este controlador eliminando el @Profile("deprecated").
 * Se cambia la ruta a "/break-glass" para coexistir limpiamente con "/emergency-login"
 * de AuthSyncController (Fallback DB local). Esto garantiza resiliencia total y segregación
 * de interfaces (ADR-001) para operaciones de auditoría estricta en caso de fallo catastrófico de BD.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class EmergencyLoginController {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityAuditLogRepository securityAuditLogRepository;

    @Value("${security.breakglass.secret:emergency-secret-key-123}")
    private String emergencySecret;

    public EmergencyLoginController(JwtTokenProvider jwtTokenProvider, SecurityAuditLogRepository securityAuditLogRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityAuditLogRepository = securityAuditLogRepository;
    }

    @PostMapping("/break-glass")
    public ResponseEntity<Map<String, String>> emergencyLogin(@RequestBody EmergencyLoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (!emergencySecret.equals(request.secret())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales de emergencia inválidas"));
        }
        
        if (request.justification() == null || request.justification().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "La justificación es obligatoria para Break-Glass"));
        }

        // Generar JWT temporal (Short-Lived) con rol de SUPER_ADMIN
        String emergencyToken = jwtTokenProvider.generateToken(
                "emergency_admin",
                List.of("SUPER_ADMIN"),
                "system"
        );

        // Registro exhaustivo de auditoría (Exigencia del Arquitecto)
        SecurityAuditLogEntity auditLog = new SecurityAuditLogEntity();
        auditLog.setUsername("emergency_admin");
        auditLog.setAction("BREAK_GLASS_LOGIN");
        auditLog.setIpAddress(httpRequest.getRemoteAddr());
        auditLog.setDetails("Login de emergencia activado");
        auditLog.setIsBreakGlass(true);
        auditLog.setJustification(request.justification());
        securityAuditLogRepository.save(auditLog);

        return ResponseEntity.ok(Map.of(
                "access_token", emergencyToken,
                "message", "Protocolo Break-Glass activado. Sus acciones serán auditadas."
        ));
    }

    public record EmergencyLoginRequest(String secret, String justification) {}
}
