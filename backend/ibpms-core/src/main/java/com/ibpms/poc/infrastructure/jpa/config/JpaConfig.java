package com.ibpms.poc.infrastructure.jpa.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"com.ibpms.poc", "com.ibpms.dmn"})
@EnableJpaRepositories(basePackages = {"com.ibpms.poc", "com.ibpms.dmn"})
public class JpaConfig {
}
