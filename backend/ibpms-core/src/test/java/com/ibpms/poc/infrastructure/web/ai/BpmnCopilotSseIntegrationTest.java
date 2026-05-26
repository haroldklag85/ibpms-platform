package com.ibpms.poc.infrastructure.web.ai;

import com.ibpms.poc.application.usecase.ai.BpmnLayoutAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
public class BpmnCopilotSseIntegrationTest {

    @LocalServerPort
    private int port;

    @SpyBean
    private BpmnLayoutAdapter layoutAdapter; // Queremos verificar que no consuma CPU si cae la red

    @TestConfiguration
    public static class OverrideSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
                        SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("QA_Agent", null, List.of(new SimpleGrantedAuthority("ROLE_PROCESS_ARCHITECT")))
                        );
                        filterChain.doFilter(request, response);
                    }
                }, org.springframework.security.web.context.SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    @DisplayName("SRE-01: Validar Desconexión de Red Graceful SSE (Anti-Memory Leak)")
    public void testSseEmitter_ClientDisconnect_GracefullyKillsThread() throws Exception {
        String payload = "{\"prompt\": \"Test de simulación SSE para corte abrupto.\"}";
        
        // 1. Iniciamos la petición de red REAL contra el Tomcat aleatorio
        URL url = java.net.URI.create("http://localhost:" + port + "/api/v1/api/v1/ai/copilot/generate").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setRequestProperty("Connection", "close"); // FORZAR no usar Keep-Alive
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // 2. Simulamos la recepción parcial del stream
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                System.err.println("SERVER RETURNED ERROR: " + new String(errorStream.readAllBytes(), StandardCharsets.UTF_8));
            }
            throw e;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        // Leemos las primeras lineas del SSE (data: {"fragment"...)
        reader.readLine(); // empty or metadata
        reader.readLine(); // chunk 1
        
        // 3. Simulación de Desconexión de Cliente Inmediata
        // Cortamos abruptamente el socket TCP mientras el servidor (@Async) sigue en `Thread.sleep(600)` 
        // e intenta despachar el siguiente `emitter.send()`.
        reader.close();
        is.close();
        conn.disconnect();

        // 4. Dejamos correr el tiempo suficiente para que el backend dispare el IOException (Broken Pipe)
        // en el catch interno y destruya orgánicamente la corrutina.
        TimeUnit.SECONDS.sleep(3);

        // 5. Aserción de Gobernanza SRE (Memory & Cycle Leak Prevention)
        // Demostramos que el post-procesamiento caro del BPMNDi NUNCA ocurrió.
        Mockito.verify(layoutAdapter, never()).injectMathematicalTopology(anyString());
    }
}
