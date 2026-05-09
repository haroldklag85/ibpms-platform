package com.ibpms.poc.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JwtBlacklistServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private JwtBlacklistService blacklistService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        blacklistService = new JwtBlacklistService(redisTemplate);
    }

    @Test
    void shouldRevokeSessionInRedis() {
        String userId = "user-123";
        
        blacklistService.revokeSession(userId);
        
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
        when(valueOperations.get("blacklist:user:" + userId)).thenReturn("revoked");
        
        boolean isRevoked = blacklistService.isUserRevoked(userId);
        
        assertTrue(isRevoked);
        verify(valueOperations).get("blacklist:user:" + userId);
    }

    @Test
    void shouldReturnFalseIfUserIdIsNotRevoked() {
        String userId = "user-active";
        when(valueOperations.get("blacklist:user:" + userId)).thenReturn(null);
        
        boolean isRevoked = blacklistService.isUserRevoked(userId);
        
        assertFalse(isRevoked);
    }

    @Test
    void shouldHandleRedisFailureAndFailOpen() {
        String userId = "user-fail";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis Down"));
        
        // CA-14 Policy: Fail-Open. If Redis fails, we should not block the user.
        // Unless we have local cache, but for now we expect no exception.
        boolean isRevoked = blacklistService.isUserRevoked(userId);
        
        assertFalse(isRevoked, "Should fail-open and allow access if Redis is down");
    }
}
