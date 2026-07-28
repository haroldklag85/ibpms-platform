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

    @Value("${jwt.clock-skew-seconds:0}")
    private long clockSkewSeconds;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Garantiza que la clave sea al menos 32 bytes para HS256
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generación (útil para tests) ───────────────────────────────────────────
    // @Traceability(US="US-003", CA="CA-87", DESC="Overloaded token generation for JIT provisioning claims support in tests")
    public String generateToken(String subject, List<String> roles, String tenantId) {
        return generateToken(subject, roles, tenantId, java.util.Map.of(
            "Sucursal_ID", "SUC-DEFAULT",
            "Codigo_Jefe", "J-DEFAULT"
        ));
    }

    // @Traceability(US="US-003", CA="CA-87", DESC="Token generation with custom additional claims support")
    public String generateToken(String subject, List<String> roles, String tenantId, java.util.Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);
        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("tenant_id", tenantId);
        if (additionalClaims != null) {
            additionalClaims.forEach(builder::claim);
        }
        return builder.issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateImpersonationToken(String subject, List<String> roles, String tenantId, String impersonatedBy) {
        Date now = new Date();
        long expiryTime = impersonatedBy != null ? Math.min(expirationSeconds, 1800) : expirationSeconds;
        Date actualExpiry = new Date(now.getTime() + expiryTime * 1000L);
        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("tenant_id", tenantId)
                .issuedAt(now)
                .expiration(actualExpiry)
                .signWith(secretKey, Jwts.SIG.HS256);
        if (impersonatedBy != null) {
            builder.claim("impersonatedBy", impersonatedBy);
        }
        return builder.compact();
    }

    // ── Validación y Parsing ───────────────────────────────────────────────────
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(clockSkewSeconds)
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

    public String getClaim(String token, String claimKey) {
        Object claim = parseClaims(token).get(claimKey);
        return claim != null ? claim.toString() : null;
    }

    public String getUsernameFromTokenIgnoreExpiration(String token) {
        try {
            return getSubject(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromTokenIgnoreExpiration(String token) {
        try {
            return getRoles(token);
        } catch (ExpiredJwtException e) {
            Object roles = e.getClaims().get("roles");
            if (roles instanceof List<?>) {
                return (List<String>) roles;
            }
            return List.of();
        }
    }
}
