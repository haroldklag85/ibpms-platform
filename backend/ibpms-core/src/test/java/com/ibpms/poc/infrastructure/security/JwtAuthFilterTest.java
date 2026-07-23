package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.application.service.JwtBlacklistService;
import com.ibpms.poc.application.service.security.EntraIdSyncService;
import com.ibpms.poc.application.service.security.RoleHierarchyService;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserStatus;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleDelegationRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.RoleRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @Traceability(US = "US-038", CA = {"CA-01", "CA-05"})
 * TDD: Validación de Fail-Open Degradado y RBAC Simple Aditivo.
 */
class JwtAuthFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtBlacklistService jwtBlacklistService;
    @Mock
    private RoleHierarchyService roleHierarchyService;
    @Mock
    private EntraIdSyncService entraIdSyncService;
    @Mock
    private RoleDelegationRepository roleDelegationRepository;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldFailOpen_whenRedisFails_andMethodIsGet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.getSubject("valid.jwt.token")).thenReturn("testuser");
        when(jwtBlacklistService.isTokenRevoked(anyString())).thenThrow(new RedisConnectionFailureException("Redis timeout"));
        when(jwtTokenProvider.isValid("valid.jwt.token")).thenReturn(true);
        when(jwtTokenProvider.getRoles("valid.jwt.token")).thenReturn(List.of("ibpms_rol_USER"));
        
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("testuser");
        mockUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(roleHierarchyService.resolveAllEffectiveRoles(any())).thenReturn(Set.of("USER"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldFailClosed_whenRedisFails_andMethodIsPost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.getSubject("valid.jwt.token")).thenReturn("testuser");
        when(jwtBlacklistService.isTokenRevoked(anyString())).thenThrow(new RedisConnectionFailureException("Redis timeout"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(503, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldAddMultipleRoles_whenTokenHasMultipleGroups() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.getSubject("valid.jwt.token")).thenReturn("testuser");
        when(jwtBlacklistService.isTokenRevoked(anyString())).thenReturn(false);
        when(jwtBlacklistService.isUserRevoked("testuser")).thenReturn(false);
        when(jwtTokenProvider.isValid("valid.jwt.token")).thenReturn(true);
        
        // CA-05: RBAC Aditivo
        when(jwtTokenProvider.getRoles("valid.jwt.token")).thenReturn(List.of("ibpms_rol_USER", "ibpms_rol_ADMIN"));
        
        UserEntity mockUser = new UserEntity();
        mockUser.setUsername("testuser");
        mockUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(roleHierarchyService.resolveAllEffectiveRoles(any())).thenReturn(Set.of("USER", "ADMIN"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(2, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
