package com.ibpms.poc.infrastructure.config;

import com.ibpms.poc.domain.port.out.security.SecurityRolePort;
import com.ibpms.poc.infrastructure.camunda.listener.ProcessRoleDiscoveryListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CamundaConfig {

    private final SecurityRolePort securityRolePort;

    public CamundaConfig(SecurityRolePort securityRolePort) {
        this.securityRolePort = securityRolePort;
    }

    @Bean
    public AbstractProcessEnginePlugin customParseListenerPlugin() {
        return new AbstractProcessEnginePlugin() {
            @Override
            public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
                List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();

                if (preParseListeners == null) {
                    preParseListeners = new ArrayList<>();
                    processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
                }

                preParseListeners.add(new ProcessRoleDiscoveryListener(securityRolePort));
            }
        };
    }
}
