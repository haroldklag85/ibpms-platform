package com.ibpms.poc.application.port.out;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.dto.PreFlightResultDTO;
import java.io.InputStream;
import java.util.List;

public interface BpmnValidationPort {
    PreFlightResultDTO validateDraftXml(String xml, int maxNodes);
    DeploymentValidationResponse validateBpmnStream(InputStream bpmnStream, List<String> activeTopics, List<String> vipRoleNames);
}
