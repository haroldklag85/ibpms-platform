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

    @Around("@annotation(com.ibpms.poc.infrastructure.web.annotation.SandboxOperation)")
    public Object enforceSandboxLimits(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // CA-63: Validar que el entorno está en modo Sandbox
        String sandboxHeader = request.getHeader("X-Sandbox-Mode");
        if (sandboxHeader == null || !sandboxHeader.equalsIgnoreCase("true")) {
            throw new IllegalStateException("Esta operación requiere modo Sandbox activo (X-Sandbox-Mode=true).");
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
