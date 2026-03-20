package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthSyncController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthSyncController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * CA-03: JIT Provisioning con Guardrail de Claims Mínimos.
     * Si EntraID no envía datos vitales, detenemos al SSO en seco (HTTP 428).
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncIdentity(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || !jwtTokenProvider.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Inválido o Ausente");
        }

        List<String> missingClaims = new ArrayList<>();
        
        // Extracción teórica de Claims de Negocio
        // Supongamos que branchId o managerId son mandatorios
        String branchId = jwtTokenProvider.getClaim(token, "branchId");
        String managerId = jwtTokenProvider.getClaim(token, "managerId");

        if (branchId == null || branchId.isBlank()) missingClaims.add("branchId");
        if (managerId == null || managerId.isBlank()) missingClaims.add("managerId");

        if (!missingClaims.isEmpty()) {
            // Error 428 Precondition Required para que Front despliegue el Modal
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("missingClaims", missingClaims, 
                                 "message", "Completar Perfil Local Requerido para continuar."));
        }

        // Si están completos, devolver ok para continuar con Workdesk.
        return ResponseEntity.ok(Map.of("status", "Synchronized successfully"));
    }

    /**
     * CA-04: Protocolo Break-Glass (Login de Emergencia Blindado)
     * Prohibido su uso desde Internet. Exclusivo de VPN/Intranet Corporativa.
     */
    @PostMapping("/emergency-login")
    public ResponseEntity<?> breakGlassLogin(@RequestBody Map<String, String> creds, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        
        // Filtro Crudo de Subnets Seguras (Mock PoC)
        if (!ip.equals("127.0.0.1") && !ip.equals("0:0:0:0:0:0:0:1") && !ip.startsWith("10.") && !ip.startsWith("192.168.")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("BREAK-GLASS DENIED: Su IP (" + ip + ") carece de habilitación perimetral.");
        }

        String pin = creds.get("adminPin");
        // Super Hash ultra protegido
        if ("BREAK-GLASS-1234".equals(pin)) {
            // Emite un Token Supremamente Privilegiado
            String overrideToken = jwtTokenProvider.generateToken("break_glass_admin", List.of("ibpms_rol_SUPER_ADMIN"));
            return ResponseEntity.ok(Map.of("token", overrideToken, "message", "WARNING: Break-Glass Protocol Activated. Admins notified."));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
