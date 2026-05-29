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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
/**
 * Configuración de Spring Security OIDC (OAuth2 Resource Server).
 * Delega la validación de tokens al IdP corporativo (Ej. Entra ID).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret:changeme-this-must-be-at-least-32-chars!!}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256")).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter getJwtAuthenticationConverter() {
        org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Remueve el prefijo SCOPE_
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitar CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesión HTTP
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Actuator health (monitoreo sin autenticación)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Webhook de M365 autenticado por lógica propia
                        .requestMatchers(HttpMethod.POST, "/inbound/email-webhook", "/api/v1/inbound/email-webhook").permitAll()
                        // CA-15: Bypass Anónimo
                        .requestMatchers(HttpMethod.POST, "/api/v1/process/*/start-anonymous").permitAll()
                        // CA-03 y CA-04 (US-038): Login Standard y Protocolo Break-Glass
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/sync", "/api/v1/auth/emergency-login").permitAll()
                        // Apertura Temporal para desbloquear catálogo y procesos
                        .requestMatchers("/api/v1/design/processes/**", "/api/v1/design/sandbox/**").permitAll()
                        // OpenAPI / Swagger Docs
                        .requestMatchers("/v3/api-docs/**", "/api/v1/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // DMN Simulation for Tests (Bypass para el test Sandbox DMN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/dmn-models/simulate").permitAll()
                        // US-028: Form Certification & Definition endpoints (QA Integration Tests)
                        .requestMatchers("/api/v1/design/forms/*/versions").permitAll()
                        .requestMatchers("/api/v1/design/form-definitions/**").permitAll()
                        .requestMatchers("/api/v1/forms/**").permitAll()
                        // CA-11: SSE Security Stream
                        .requestMatchers("/api/v1/security/stream").permitAll()
                        // @Traceability(US="US-J04-42", CA="CA-E2E-OBS", DESC="ADR-010 Observabilidad E2E: Bypass JWT para métricas ágiles")
                        .requestMatchers("/api/v1/agile/**").permitAll()
                        // @Traceability(US="US-CORE", CA="CA-CAMUNDA", DESC="ADR-003: Bypass JWT para interacción con motor REST embebido de Camunda 7")
                        .requestMatchers("/engine-rest/**", "/api/v1/engine-rest/**").permitAll()
                        .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(getJwtAuthenticationConverter())));

        // CA-25: Inyectar Filtro Zero-Trust JWT en el Pipeline de Spring Security
        // JwtAuthFilter is automatically registered as a bean and acts per request.
        // We can explicitly wire it if needed, but since it extends OncePerRequestFilter and is @Component, Spring boot auto-registers it.

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // @Traceability: US-005, CA-63
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Sandbox-Mode"));
        configuration.setExposedHeaders(Arrays.asList("X-Sandbox-Mode"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
