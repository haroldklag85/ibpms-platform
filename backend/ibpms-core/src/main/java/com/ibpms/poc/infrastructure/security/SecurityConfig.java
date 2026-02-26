package com.ibpms.poc.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 * – Stateless (sin sesión HTTP ni cookies de sesión).
 * – JWT Bearer Token obligatorio en todos los endpoints protegidos.
 * – Rutas públicas: /actuator/health, /api/v1/inbound/email-webhook
 * (autenticado por ClientState).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

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
                        // Webhook de M365 — autenticado por cabecera ClientState, no por JWT
                        .requestMatchers(HttpMethod.POST, "/api/v1/inbound/email-webhook").permitAll()
                        // Camunda Cockpit y APIs internas (si se exponen en el mismo server)
                        .requestMatchers("/engine-rest/**").hasRole("ADMIN")
                        // Todo lo demás requiere autenticación JWT
                        .anyRequest().authenticated())

                // Añadir filtro JWT antes del filtro estándar de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
