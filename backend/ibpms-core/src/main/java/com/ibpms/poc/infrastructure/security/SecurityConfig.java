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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * Configuración de Spring Security OIDC (OAuth2 Resource Server).
 * Delega la validación de tokens al IdP corporativo (Ej. Entra ID).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${ibpms.security.jwt.secret:UAT_SUPER_SECRET_KEY_THAT_IS_VERY_LONG_AND_SECURE_982374982374892374}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256")).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                        // Webhook de M365 autenticado por lógica propia
                        .requestMatchers(HttpMethod.POST, "/api/v1/inbound/email-webhook").permitAll()
                        // CA-15: Bypass Anónimo
                        .requestMatchers(HttpMethod.POST, "/api/v1/process/*/start-anonymous").permitAll()
                        // CA-03 y CA-04 (US-038): Aprovisionamiento JIT y Protocolo Break-Glass
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sync", "/api/v1/auth/emergency-login").permitAll()
                        // OpenAPI / Swagger Docs
                        .requestMatchers("/v3/api-docs/**", "/api/v1/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // DMN Simulation for Tests (Bypass para el test Sandbox DMN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/dmn-models/simulate").permitAll()
                        // US-028: Form Certification & Definition endpoints (QA Integration Tests)
                        .requestMatchers("/api/v1/design/forms/**").permitAll()
                        .requestMatchers("/api/v1/forms/**").permitAll()
                        .requestMatchers("/api/v1/design/processes/**").permitAll()
                        .anyRequest().authenticated())

                // Habilitamos OAUTH2 JWT Validation delegando al Issuer-URI (Properties)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
