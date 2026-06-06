package com.ibpms.poc.infrastructure.web.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Traceability: US-001, CA-30 (Rate Limiting Preventivo)
 * Desplaza la lógica de Bucket4j fuera del Controlador REST (ADR-001).
 */
@Component
public class WorkdeskRateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String userId) {
        return userBuckets.computeIfAbsent(userId, k -> {
            Bandwidth limit = Bandwidth.builder().capacity(60).refillGreedy(60, Duration.ofMinutes(1)).build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/v1/workdesk/global-inbox")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String rateLimitKey = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
            
            if (!resolveBucket(rateLimitKey).tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests (CA-30)\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
