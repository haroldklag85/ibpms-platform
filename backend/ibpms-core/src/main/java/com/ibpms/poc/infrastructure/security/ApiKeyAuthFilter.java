package com.ibpms.poc.infrastructure.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.ServiceAccountEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private final ServiceAccountRepository serviceAccountRepository;

    public ApiKeyAuthFilter(ServiceAccountRepository serviceAccountRepository) {
        this.serviceAccountRepository = serviceAccountRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        String rawApiKey = request.getHeader(API_KEY_HEADER);

        if (rawApiKey != null && !rawApiKey.isBlank()) {
            try {
                // Generar Hash M2M para buscar
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(rawApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder(2 * hash.length);
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String hashedKey = hexString.toString();

                Optional<ServiceAccountEntity> optAcc = serviceAccountRepository.findByApiKeyHashAndIsActiveTrue(hashedKey);

                if (optAcc.isPresent()) {
                    ServiceAccountEntity acc = optAcc.get();
                    // Conceder Sesión Sintética M2M
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority(acc.getRole().getName()));
                    var auth = new UsernamePasswordAuthenticationToken(acc.getName() + "_M2M", null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key Inválida o Cuenta Inactiva.");
                    return;
                }

            } catch (NoSuchAlgorithmException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SHA-256 no soportado.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
