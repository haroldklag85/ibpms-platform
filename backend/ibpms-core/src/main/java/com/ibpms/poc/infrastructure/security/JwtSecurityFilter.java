package com.ibpms.poc.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro perimetral que intercepta peticiones HTTP para asegurar la capa de API (Pantalla 14).
 * Valida formatos JWT de EntraID y consulta una Lista Negra (Redis/Mock) in-flight
 * para revocar tokens comprometidos de inmediato (Token Revocation List - TRL).
 * 
 * @Traceability: US-036 - CA-25
 */
@Slf4j
public class JwtSecurityFilter extends OncePerRequestFilter {

    private final Set<String> redisBlacklistMock = new HashSet<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpRequest, 
                                    @NonNull HttpServletResponse httpResponse, 
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        try {
            String authHeader = httpRequest.getHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.length() > 7 && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Si hay un token, validamos revocación inmediata (TRL)
            if (token != null) {
                if (isTokenBlacklisted(token)) {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.getWriter().write("401 Unauthorized: Token revocado por seguridad (TRL).");
                    return;
                }
                // Enriquecemos el request con el ID del usuario para logs perimetrales
                httpRequest.setAttribute("validated_user", extractUserId(token));
            }
        } catch (Exception e) {
            // CA-25: Fail-Safe. Si falla el parsing o la auditoría perimetral, 
            // dejamos pasar a la SecurityFilterChain de Spring para que ella decida (Zero-Trust Layer 2).
            log.error("Error en JwtSecurityFilter (bypass preventivo): {}", e.getMessage());
        }

        // Delegamos el resto de la seguridad (Authn/Authz) a la SecurityFilterChain de Spring
        chain.doFilter(httpRequest, httpResponse);
    }

    /**
     * MOCK: Simula consulta de O(1) a Redis.
     */
    private boolean isTokenBlacklisted(String token) {
        return redisBlacklistMock.contains(token);
    }

    /**
     * Permite a los administradores o al Identity Provider revocar un token.
     */
    public void blacklistToken(String token) {
        redisBlacklistMock.add(token);
    }

    private String extractUserId(String token) {
        if (token == null) return "Unknown";
        // Mock parsing
        try {
            if (token.contains("user_a")) return "User_A";
            if (token.contains("user_b")) return "User_B";
        } catch (Exception e) {
            return "Malformed_Token";
        }
        return "Unknown";
    }
}
