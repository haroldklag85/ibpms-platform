package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.dto.PreFlightResultDTO;
import com.ibpms.poc.application.port.out.BpmnAuditPort;
import com.ibpms.poc.application.port.out.BpmnValidationPort;
import com.ibpms.poc.application.port.out.ExternalTaskTopicPort;
import com.ibpms.poc.application.port.out.SecurityRolePort;
import com.ibpms.poc.domain.model.BpmnProcessDesign;
import com.ibpms.poc.domain.model.ExternalTaskTopic;
import com.ibpms.poc.domain.model.security.SecurityRole;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Pre-Flight Analyzer — CA-9, CA-24.
 * Parsea el XML BPMN del borrador y valida reglas estructurales.
 */
@Service
public class PreFlightAnalyzerService {

    private final BpmnDesignService designService;
    private final BpmnValidationPort bpmnValidationPort;
    private final BpmnAuditPort bpmnAuditPort;
    private final SecurityRolePort securityRolePort;
    private final ExternalTaskTopicPort externalTaskTopicPort;

    public PreFlightAnalyzerService(BpmnDesignService designService,
                                    BpmnValidationPort bpmnValidationPort,
                                    BpmnAuditPort bpmnAuditPort,
                                    SecurityRolePort securityRolePort,
                                    ExternalTaskTopicPort externalTaskTopicPort) {
        this.designService = designService;
        this.bpmnValidationPort = bpmnValidationPort;
        this.bpmnAuditPort = bpmnAuditPort;
        this.securityRolePort = securityRolePort;
        this.externalTaskTopicPort = externalTaskTopicPort;
    }

    public PreFlightResultDTO analizar(UUID processDesignId, String userId) {
        // Obtenemos el domain model en vez de la entidad
        BpmnProcessDesign design = designService.getDomainModel(processDesignId);
        String xml = design.getXmlDraft();

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("No hay borrador XML para analizar.");
        }

        PreFlightResultDTO result = bpmnValidationPort.validateDraftXml(xml, design.getMaxNodes());

        // Audit log usando el Puerto
        bpmnAuditPort.logAction(
                processDesignId, 
                "PRE_FLIGHT", 
                userId,
                design.getCurrentVersion(),
                "{\"passed\":" + result.isPassed() + ",\"issues\":" + result.getIssues().size() + "}"
        );

        return result;
    }

    /**
     * CA-1 a CA-4: Análisis semántico en caliente usando Camunda BPMN Model API.
     */
    public DeploymentValidationResponse analizar(InputStream bpmnStream) {
        List<String> vipRoleNames = securityRolePort.findByIsVipRestrictedTrue().stream()
                .map(SecurityRole::getName)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        List<String> activeTopics = externalTaskTopicPort.findByIsActiveTrue().stream()
                .map(ExternalTaskTopic::getTopicName)
                .collect(Collectors.toList());

        return bpmnValidationPort.validateBpmnStream(bpmnStream, activeTopics, vipRoleNames);
    }
}
