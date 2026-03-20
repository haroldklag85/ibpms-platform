package com.ibpms.poc.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;

/**
 * CA-09: Trazabilidad Quirúrgica (X-Trace-ID).
 * Filtra TODA petición HTTP entrante, asegurando que cada request 
 * adquiera un Correlation ID único que SLF4J incrustará en cada línea de Log.
 */
@Component
public class MdcLogFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Extraer Trace-ID del header o generar uno nuevo si es el origen de la invocación
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // Inyectar en Mapped Diagnostic Context de Logback
        MDC.put(MDC_TRACE_ID_KEY, traceId);

        // Propagar el Trace-ID hacia el Frontend (Útil para depurar Soporte Técnico)
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Mandatorio: Prevenir "Leak" de memoria en hilos re-usados por Tomcat
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
