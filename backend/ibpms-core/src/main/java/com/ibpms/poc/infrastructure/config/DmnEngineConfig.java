package com.ibpms.poc.infrastructure.config;

import com.ibpms.dmn.GenerarDmnAiService;
import com.ibpms.dmn.GenerarDmnAiUseCase;
import com.ibpms.dmn.NlpAgentPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DmnEngineConfig {

    @Bean
    public GenerarDmnAiUseCase generarDmnAiUseCase(NlpAgentPort nlpAgentPort) {
        return new GenerarDmnAiService(nlpAgentPort);
    }
}
