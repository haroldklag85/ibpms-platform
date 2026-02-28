package com.ibpms.poc.infrastructure.config;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaversConfig {

    @Bean
    public AuthorProvider provideJaversAuthor() {
        return () -> {
            // En V2 usará SecurityContextHolder para el OIDC/JWT
            // En V1 PoC delegaremos en un nombre por defecto del sistema
            return "system_po_user";
        };
    }
}
