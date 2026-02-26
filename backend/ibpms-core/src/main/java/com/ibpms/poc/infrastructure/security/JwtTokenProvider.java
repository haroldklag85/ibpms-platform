package com.ibpms.poc.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Componente para generar y validar JSON Web Tokens (JWT).
 * Usa JJWT 0.12.x — firma HS256 con clave secreta configurable.
 * No depende de Spring Security directamente: puro servicio de parsing.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:changeme-this-must-be-at-least-32-chars!!}")
    private String secretString;

    @Value("${jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Garantiza que la clave sea al menos 32 bytes para HS256
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generación (útil para tests) ───────────────────────────────────────────
    public String generateToken(String subject, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // ── Validación y Parsing ───────────────────────────────────────────────────
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }
}
