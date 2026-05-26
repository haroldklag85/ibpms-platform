package com.ibpms.poc.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test Suite for JwtBlacklistService.
 * @Traceability(US="US-010", CA="CA-14", DESC="ADR-010: Testing Pyramid. Pruebas unitarias para revocación JTI, UserId y resiliencia Fail-Open.")
 */
@ExtendWith(MockitoExtension.class)
class JwtBlacklistServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() {
        // Mock opsForValue only when needed to prevent UnnecessaryStubbingException
    }

    // ==========================================
    // PRUEBAS DE HEAD (Token por JTI con TTL)
    // ==========================================

    @Test
    void shouldBlacklistTokenSuccessfully() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        
        jwtBlacklistService.blacklistToken("test-jti-123", 3600);

        verify(valueOperations, times(1)).set("blacklist:token:test-jti-123", "revoked", 3600, TimeUnit.SECONDS);
    }

    @Test
    void isRevokedShouldReturnTrueWhenKeyExists() {
        when(stringRedisTemplate.hasKey("blacklist:token:test-jti-123")).thenReturn(true);
        
        boolean revoked = jwtBlacklistService.isTokenRevoked("test-jti-123");

        assertTrue(revoked);
        verify(stringRedisTemplate, times(1)).hasKey("blacklist:token:test-jti-123");
    }

    @Test
    void isRevokedShouldReturnFalseWhenKeyDoesNotExist() {
        when(stringRedisTemplate.hasKey("blacklist:token:test-jti-456")).thenReturn(false);
        
        boolean revoked = jwtBlacklistService.isTokenRevoked("test-jti-456");

        assertFalse(revoked);
    }

    @Test
    void isRevokedShouldReturnFalseWhenHasKeyReturnsNull() {
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(null);
        
        boolean revoked = jwtBlacklistService.isTokenRevoked("test-jti-null");

        assertFalse(revoked);
    }

    // ==========================================
    // PRUEBAS DE DEV-DAVID (Sesiones y Fail-Open)
    // ==========================================

    // @Traceability(US="US-010", CA="CA-14", DESC="Asegurar expiración global de sesiones por UserId.")
    @Test
    void shouldRevokeSessionInRedis() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String userId = "user-123";
        
        jwtBlacklistService.revokeSession(userId);
        
        verify(valueOperations).set(
                eq("blacklist:user:" + userId),
                eq("revoked"),
                eq(24L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void shouldReturnTrueIfUserIdIsRevokedInRedis() {
        String userId = "user-123";
        when(stringRedisTemplate.hasKey("blacklist:user:" + userId)).thenReturn(true);
        
        boolean isRevoked = jwtBlacklistService.isUserRevoked(userId);
        
        assertTrue(isRevoked);
    }

    @Test
    void shouldReturnFalseIfUserIdIsNotRevoked() {
        String userId = "user-active";
        when(stringRedisTemplate.hasKey("blacklist:user:" + userId)).thenReturn(false);
        
        boolean isRevoked = jwtBlacklistService.isUserRevoked(userId);
        
        assertFalse(isRevoked);
    }

    // @Traceability(US="US-010", CA="CA-14", DESC="ADR-013/CA-14: Política Fail-Open. El sistema NO bloquea usuarios si Redis colapsa.")
    @Test
    void shouldHandleRedisFailureAndFailOpen() {
        // Configurar simulación de caída catastrófica de Redis
        String userId = "user-fail";
        when(stringRedisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis Down"));
        
        // CA-14 Policy: Fail-Open. If Redis fails, we should not block the user.
        boolean isRevoked = jwtBlacklistService.isUserRevoked(userId);
        
        assertFalse(isRevoked, "Should fail-open and allow access if Redis is down");
    }
}
