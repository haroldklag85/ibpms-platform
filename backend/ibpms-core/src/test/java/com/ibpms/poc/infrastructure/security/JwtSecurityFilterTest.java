package com.ibpms.poc.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JwtSecurityFilterTest {

    private final JwtSecurityFilter filter = new JwtSecurityFilter();

    @Test
    @DisplayName("Debe permitir el paso 200 OK de una petición con token EntraID válido")
    void doFilter_ValidToken_CallsFilterChain() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getRequestURI()).thenReturn("/api/v1/tasks");
        when(req.getHeader("Authorization")).thenReturn("Bearer active_user_a_token");

        filter.doFilter(req, res, chain);

        // Verifica que se delegó la cadena de filtros sin interrupción
        verify(chain, times(1)).doFilter(req, res);
        verify(res, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Debe denegar forzosamente (401) la petición si el ID del EntraID JWT fue marcado en la Lista Negra de Redis")
    void doFilter_BlacklistedToken_Returns401Unauthorized() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // 1. Arrange: Un token se intercepta
        String evilToken = "revoked_hacker_token";
        when(req.getRequestURI()).thenReturn("/api/v1/tasks");
        when(req.getHeader("Authorization")).thenReturn("Bearer " + evilToken);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(writer);

        // 2. Act: El admin lo añade a la lista negra (Mocked Redis)
        filter.blacklistToken(evilToken);

        // 3. Act: El atacante intenta usarlo de nuevo
        filter.doFilter(req, res, chain);

        // 4. Assert: Fue interceptado en la puerta (Perimetral)
        verify(res, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(req, res);
        assertTrue(stringWriter.toString().contains("revocado (Blacklisted)"));
    }
}
