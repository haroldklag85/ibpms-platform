package com.ibpms.poc.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

/**
 * Configuración de Spring Security OIDC (OAuth2 Resource Server).
 * Delega la validación de tokens al IdP corporativo (Ej. Entra ID).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesión HTTP
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Actuator health (monitoreo sin autenticación)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Webhook de M365 autenticado por lógica propia
                        .requestMatchers(HttpMethod.POST, "/api/v1/inbound/email-webhook").permitAll()
                        .anyRequest().authenticated())

                // Habilitamos OAUTH2 JWT Validation delegando al Issuer-URI (Properties)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
