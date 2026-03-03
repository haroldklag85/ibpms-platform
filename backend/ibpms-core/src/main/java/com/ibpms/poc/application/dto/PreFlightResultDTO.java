package com.ibpms.poc.application.dto;

import java.util.ArrayList;
import java.util.List;

public class PreFlightResultDTO {

    public enum Severity {
        ERROR, WARNING
    }

    private boolean passed;
    private List<Issue> issues = new ArrayList<>();

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public void addIssue(Severity severity, String rule, String nodeId, String message) {
        issues.add(new Issue(severity.name(), rule, nodeId, message));
    }

    public static class Issue {
        private String severity;
        private String rule;
        private String nodeId;
        private String message;

        public Issue(String severity, String rule, String nodeId, String message) {
            this.severity = severity;
            this.rule = rule;
            this.nodeId = nodeId;
            this.message = message;
        }

        public String getSeverity() {
            return severity;
        }

        public String getRule() {
            return rule;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getMessage() {
            return message;
        }
    }
}
