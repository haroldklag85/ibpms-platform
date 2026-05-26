package com.ibpms.poc.infrastructure.web.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
public class SandboxInterceptor {

    private final StringRedisTemplate redisTemplate;
    // Maximum concurrent simulated instances allowed
    private static final int MAX_SANDBOX_INSTANCES = 3;
    private static final String REDIS_SANDBOX_COUNTER_KEY = "sandbox_active_simulations";

    public SandboxInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // @Traceability: US-005, CA-63 Modo Sandbox, CA-67 Aislamiento DoS Sandbox
    @Around("@annotation(com.ibpms.poc.infrastructure.web.annotation.SandboxOperation)")
    public Object enforceSandboxLimits(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // CA-63: Validar que el entorno está en modo Sandbox
        String sandboxHeader = request.getHeader("X-Sandbox-Mode");
        if (sandboxHeader == null || !sandboxHeader.equalsIgnoreCase("true")) {
            throw new IllegalStateException("Esta operación requiere modo Sandbox activo (X-Sandbox-Mode=true).");
        }

        // CA-67: Control de Payload (Max 2MB)
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof org.springframework.web.multipart.MultipartFile) {
                org.springframework.web.multipart.MultipartFile file = (org.springframework.web.multipart.MultipartFile) arg;
                if (file.getSize() > 2 * 1024 * 1024) { // 2MB
                    throw new com.ibpms.poc.domain.exception.PayloadTooLargeException("El archivo excede el límite de Sandbox (2MB).");
                }
            }
        }

        // CA-67: Rate Limiting (Máx 10 peticiones por minuto)
        String clientIp = request.getRemoteAddr();
        String rateLimitKey = "sandbox_rate_limit:" + clientIp;
        Long reqCount = redisTemplate.opsForValue().increment(rateLimitKey);
        if (reqCount != null && reqCount == 1) {
            redisTemplate.expire(rateLimitKey, Duration.ofMinutes(1));
        }
        if (reqCount != null && reqCount > 10) {
            throw new com.ibpms.poc.domain.exception.TooManyRequestsException("Rate limit de Sandbox superado (10 req/min).");
        }

        // CA-67: Control de Límite (Max 3 simulaciones)
        Long currentCount = redisTemplate.opsForValue().increment(REDIS_SANDBOX_COUNTER_KEY);
        if (currentCount != null && currentCount == 1) {
            // Expire entirely if it was just created
            redisTemplate.expire(REDIS_SANDBOX_COUNTER_KEY, Duration.ofMinutes(15));
        }

        if (currentCount != null && currentCount > MAX_SANDBOX_INSTANCES) {
            redisTemplate.opsForValue().decrement(REDIS_SANDBOX_COUNTER_KEY); // Revert
            throw new com.ibpms.poc.domain.exception.ResourceExhaustedException("Límite de Sandbox superado (" + MAX_SANDBOX_INSTANCES + " instancias permitidas).");
        }

        try {
            return joinPoint.proceed();
        } finally {
            // Decrementar a la salida
            redisTemplate.opsForValue().decrement(REDIS_SANDBOX_COUNTER_KEY);
        }
    }
}
