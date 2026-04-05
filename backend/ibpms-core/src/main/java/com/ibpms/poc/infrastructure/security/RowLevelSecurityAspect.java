package com.ibpms.poc.infrastructure.security;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RowLevelSecurityAspect {

    private static final Logger log = LoggerFactory.getLogger(RowLevelSecurityAspect.class);
    private final EntityManager entityManager;

    public RowLevelSecurityAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository.*(..))")
    public void enableAssigneeFilter(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String currentUser = auth.getName();
            
            // Si el usuario es ADMIN_IT u otro rol superUser, podríamos saltar el filtro.
            // Pero como la US-036 exige RLS estricto para el Workdesk (CA-20), lo aplicaremos incondicionalmente para la bandeja operativa.
            log.debug("AOP RLS: Habilitando assigneeSecurityFilter para el usuario [{}] en la consulta: {}", currentUser, joinPoint.getSignature().getName());
            
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("assigneeSecurityFilter").setParameter("currentUserId", currentUser);
        } else {
            log.warn("AOP RLS: Intento de consulta al Workdesk sin contexto de seguridad establecido");
        }
    }
}
