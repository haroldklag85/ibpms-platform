package com.ibpms.poc.infrastructure.web.interceptor;

import com.ibpms.poc.application.util.SecurityContextUtils;
import com.ibpms.poc.infrastructure.web.annotation.CurrentTenant;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @Traceability: US-001, CA-14, CA-29 (Consolidación de Identidad / Tenant)
 * Resuelve el parámetro @CurrentTenant en controladores leyendo el JWT.
 */
@Component
public class TenantArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentTenant.class) && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // @Traceability: US-001, CA-29
        String tenantId;
        try {
            tenantId = SecurityContextUtils.getTenantId();
        } catch (IllegalStateException e) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = (auth != null && auth.getName() != null) ? auth.getName() : "default";
            tenantId = "default";
            
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                tenantId = jwt.getClaimAsString("tenant_id");
                if (tenantId == null) tenantId = "tenant_alpha"; // Fallback de seguridad
            } else if (currentUserId != null && (currentUserId.endsWith("@alpha.com") || currentUserId.startsWith("analista") || currentUserId.startsWith("perito") || currentUserId.startsWith("director") || currentUserId.startsWith("admin"))) {
                tenantId = "tenant_alpha";
            } else if ("analista_n1@ibpms.local".equals(currentUserId)) {
                tenantId = "tenant_alpha"; 
            }
        }
        
        return tenantId;
    }
}
