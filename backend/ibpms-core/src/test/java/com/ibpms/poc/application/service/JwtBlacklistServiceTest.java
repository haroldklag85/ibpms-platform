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
        // mock opsForValue only when needed
    }

    @Test
    void shouldBlacklistTokenSuccessfully() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        
        jwtBlacklistService.blacklistToken("test-jti-123", 3600);

        verify(valueOperations, times(1)).set("blacklist:token:test-jti-123", "revoked", 3600, TimeUnit.SECONDS);
    }

    @Test
    void isRevokedShouldReturnTrueWhenKeyExists() {
        when(stringRedisTemplate.hasKey("blacklist:token:test-jti-123")).thenReturn(true);
        
        boolean revoked = jwtBlacklistService.isRevoked("test-jti-123");

        assertTrue(revoked);
        verify(stringRedisTemplate, times(1)).hasKey("blacklist:token:test-jti-123");
    }

    @Test
    void isRevokedShouldReturnFalseWhenKeyDoesNotExist() {
        when(stringRedisTemplate.hasKey("blacklist:token:test-jti-456")).thenReturn(false);
        
        boolean revoked = jwtBlacklistService.isRevoked("test-jti-456");

        assertFalse(revoked);
        verify(stringRedisTemplate, times(1)).hasKey("blacklist:token:test-jti-456");
    }

    @Test
    void isRevokedShouldReturnFalseWhenHasKeyReturnsNull() {
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(null);
        
        boolean revoked = jwtBlacklistService.isRevoked("test-jti-null");

        assertFalse(revoked);
    }
}
