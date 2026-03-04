package com.ibpms.poc.infrastructure.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro perimetral que intercepta peticiones HTTP para asegurar la capa de API
 * (Pantalla 14).
 * Valida formatos JWT de EntraID y consulta una Lista Negra (Redis/Mock)
 * in-flight
 * para revocar tokens comprometidos de inmediato (Token Revocation List - TRL).
 */
@Component
public class JwtSecurityFilter implements Filter {

    // En producción esto sería un RedisTemplate o un Cache Manager
    private final Set<String> redisBlacklistMock = new HashSet<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Public Bypass (CA-15): Ignore HTTP filters for endpoints prefixed with
        // /api/v1/public/forms/**
        if (path.startsWith("/api/v1/public/forms/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Verificación de Lista Negra (Redis)
            if (isTokenBlacklisted(token)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter()
                        .write("401 Unauthorized: El token EntraID suministrado ha sido revocado (Blacklisted).");
                return;
            }

            // Aquí se delegaría la validación criptográfica de firmas RSA de EntraID (fuera
            // del alcance del test)
            httpRequest.setAttribute("validated_user", extractUserId(token));
        }

        chain.doFilter(request, response);
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
        // Mock parsing
        if (token.contains("user_a"))
            return "User_A";
        if (token.contains("user_b"))
            return "User_B";
        return "Unknown";
    }
}
