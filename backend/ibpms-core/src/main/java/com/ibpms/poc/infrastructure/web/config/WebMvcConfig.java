package com.ibpms.poc.infrastructure.web.config;

import com.ibpms.poc.infrastructure.web.interceptor.TenantArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Traceability: US-001, CA-14
 * Registra resolvers personalizados para inyección de dependencias limpias en los controladores.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantArgumentResolver tenantArgumentResolver;

    public WebMvcConfig(TenantArgumentResolver tenantArgumentResolver) {
        this.tenantArgumentResolver = tenantArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(tenantArgumentResolver);
    }
}
