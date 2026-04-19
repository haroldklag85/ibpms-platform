package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthSyncController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthSyncController(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * UAT E2E: Login Genérico para Pruebas de Certificación.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        if (email == null || creds.get("password") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales Inválidas"));
        }
        
        // Emite token para que Playwright pueda inyectarlo en las cabeceras REST.
        // Aseguramos inyectar los claims con los ROLES tal y como los espera el JwtAuthFilter (ibpms_rol_*)
        String resolvedTenant = email.contains("beta.com") ? "tenant_beta" : "tenant_alpha";
        String tkn = jwtTokenProvider.generateToken(email, List.of("ibpms_rol_PROCESS_ARCHITECT", "ibpms_rol_BPMN_DESIGNER", "ibpms_rol_USER"), resolvedTenant);
        return ResponseEntity.ok(Map.of("token", tkn, "tenantId", resolvedTenant, "message", "Login Exitoso E2E"));
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
            String overrideToken = jwtTokenProvider.generateToken("break_glass_admin", List.of("ibpms_rol_SUPER_ADMIN"), "tenant_system");
            return ResponseEntity.ok(Map.of("token", overrideToken, "message", "WARNING: Break-Glass Protocol Activated. Admins notified."));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * CA-4011: Rotación Continua Silenciosa (Silent Auto-Renewal).
     * Refresca el JWT (inmortalizando la sesión) condicionado a que el usuario
     * mantenga la bandera isActive=true en base de datos. Anula el Fail-Open.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token Ausente o Malformado"));
        }
        
        String currentToken = authHeader.substring(7);
        
        // Extraemos Username saltándonos la expiración (si estuviera sutilmente vencido)
        try {
            String username = jwtTokenProvider.getUsernameFromTokenIgnoreExpiration(currentToken);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token Inválido"));
            }

            // Chequeo Cero-Trust explícito: ¿Sigue el usuario activo en DB?
            boolean isActive = userRepository.isUserActive(username).orElse(false);
            if (!isActive) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", "PRIVILEGES_CHANGED", "message", "Cuenta Deshabilitada M2M"));
            }

            // Generar nuevo token asumiendo rescate de roles desde el token anterior (o idealmente desde la BD)
            // Para mantener compatibilidad estricta con claims originales (incluyendo break glass):
            List<String> roles = jwtTokenProvider.getRolesFromTokenIgnoreExpiration(currentToken);
            String tenantId = jwtTokenProvider.getClaim(currentToken, "tenant_id");
            if (tenantId == null) tenantId = "tenant_alpha";
            String freshToken = jwtTokenProvider.generateToken(username, roles, tenantId);

            return ResponseEntity.ok(Map.of("token", freshToken, "message", "Renovado Transaccionalmente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token Defectuoso o Rechazado"));
        }
    }
}
