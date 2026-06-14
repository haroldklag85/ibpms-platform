package com.ibpms.poc.application.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utilidad transversal para la extracción homogeneizada del contexto de seguridad (Spring Security).
 * Previene la fragmentación de lógica (IDOR prevention) a lo largo del Core.
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {}

    /**
     * Extrae el identificador del usuario autenticado (Assignee).
     * @return username del contexto o "system" si no hay nadie.
     */
    public static String getAssignee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }
        // Exclusión para principal anónimo o machine-to-machine
        if ("anonymousUser".equals(auth.getName())) {
            return "system";
        }
        return auth.getName();
    }

    /**
     * Extrae el identificador corporativo (tenant_id) del token JWT activo.
     * Crucial para prevenir Data Leakage inter-inquilino.
     * @return El Tenant ID.
     * @throws IllegalStateException Si no existe `tenant_id` en el contexto.
     */
    public static String getTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            String tenantId = jwtToken.getToken().getClaimAsString("tenant_id");
            if (tenantId != null && !tenantId.isBlank()) {
                return tenantId;
            }
        } else if (auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken upToken) {
            if (upToken.getDetails() instanceof java.util.Map<?, ?> detailsMap) {
                Object tenantId = detailsMap.get("tenant_id");
                if (tenantId != null && !tenantId.toString().isBlank()) {
                    return tenantId.toString();
                }
            }
        }
        
        throw new IllegalStateException("SecurityContext no provee un Tenant_ID válido desde el JWT.");
    }
}
