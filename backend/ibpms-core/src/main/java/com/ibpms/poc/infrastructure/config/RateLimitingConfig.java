package com.ibpms.poc.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    /**
     * CA-14: Configuración de rate-limiting para borradores (6 req / min)
     */
    @Bean
    public Bucket draftRateLimiterBucket() {
        Bandwidth limit = Bandwidth.classic(6, Refill.intervally(6, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
