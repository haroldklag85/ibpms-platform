package com.ibpms.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = {"com.ibpms.poc", "com.ibpms.dmn"})
@EnableScheduling
@EnableAsync
@EnableRetry
@EntityScan(basePackages = {"com.ibpms.poc", "com.ibpms.dmn"})
@EnableJpaRepositories(basePackages = {"com.ibpms.poc", "com.ibpms.dmn"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
