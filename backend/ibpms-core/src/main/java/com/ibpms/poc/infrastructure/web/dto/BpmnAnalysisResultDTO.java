package com.ibpms.poc.infrastructure.web.dto;

import java.util.List;

public class BpmnAnalysisResultDTO {
    private List<BpmnFinding> criticalIsoViolations;
    private List<BpmnFinding> structuralAntiPatterns;
    private List<BpmnFinding> automationOpportunities;
    private int internalScore1To100;

    public List<BpmnFinding> getCriticalIsoViolations() {
        return criticalIsoViolations;
    }

    public void setCriticalIsoViolations(List<BpmnFinding> criticalIsoViolations) {
        this.criticalIsoViolations = criticalIsoViolations;
    }

    public List<BpmnFinding> getStructuralAntiPatterns() {
        return structuralAntiPatterns;
    }

    public void setStructuralAntiPatterns(List<BpmnFinding> structuralAntiPatterns) {
        this.structuralAntiPatterns = structuralAntiPatterns;
    }

    public List<BpmnFinding> getAutomationOpportunities() {
        return automationOpportunities;
    }

    public void setAutomationOpportunities(List<BpmnFinding> automationOpportunities) {
        this.automationOpportunities = automationOpportunities;
    }

    public int getInternalScore1To100() {
        return internalScore1To100;
    }

    public void setInternalScore1To100(int internalScore1To100) {
        this.internalScore1To100 = internalScore1To100;
    }
}
