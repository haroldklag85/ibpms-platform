// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.web;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import com.ibpms.poc.infrastructure.security.JwtTokenProvider;
import com.ibpms.poc.infrastructure.security.JwtAuthFilter;
import com.ibpms.poc.infrastructure.security.ApiKeyAuthFilter;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;

import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
public abstract class BaseWebMvcIT {

    @MockBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected JwtAuthFilter jwtAuthFilter;

    @MockBean
    protected ApiKeyAuthFilter apiKeyAuthFilter;

    @MockBean
    protected ServiceAccountRepository serviceAccountRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUpSecurityFilters() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(apiKeyAuthFilter).doFilter(any(), any(), any());
    }
}
