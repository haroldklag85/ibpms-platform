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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthSyncController(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
     * Protocolo Emergency Login para Sprint 6.2 (Valida contra BCrypt y retorna JWT)
     */
    @PostMapping("/emergency-login")
    public ResponseEntity<?> emergencyLogin(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String rawPassword = creds.get("password");

        if (email == null || rawPassword == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "code", "MISSING_FIELDS",
                "message", "Los campos 'email' y 'password' son obligatorios."
            ));
        }

        Optional<com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "code", "USER_NOT_FOUND",
                "message", "No existe una cuenta asociada al correo proporcionado."
            ));
        }

        com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity user = userOpt.get();

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "code", "INVALID_PASSWORD",
                "message", "La contraseña proporcionada es incorrecta."
            ));
        }

        if (user.getStatus() != com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "code", "ACCOUNT_DISABLED",
                "message", "La cuenta existe pero se encuentra deshabilitada. Contacte al administrador."
            ));
        }

        // Emitir JWT con claims
        String sub = user.getUsername();
        String tenantId = email.contains("beta.com") ? "tenant_beta" : "tenant_alpha";
        
        // Asumiendo roles del usuario mapeados
        List<String> roles = user.getRoles().stream()
            .map(role -> "ibpms_rol_" + role.getName().replace("ROLE_", ""))
            .toList();

        // Fallback si no tiene roles por algún motivo
        if (roles.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("User has no roles assigned in ibpms_security_user_roles");
        }

        String overrideToken = jwtTokenProvider.generateToken(sub, roles, tenantId);
        
        return ResponseEntity.ok(Map.of("token", overrideToken, "message", "Emergency login successful"));
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
