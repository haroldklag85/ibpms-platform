package com.ibpms.poc.application.dto;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * DTO que consolida los Expedientes activos e históricos para una vista de
 * Cliente (Customer 360).
 */
public class Customer360DTO {

    private String crmId;
    private List<ProcessInfo> cases;
    private int totalCases;

    public String getCrmId() {
        return crmId;
    }

    public void setCrmId(String crmId) {
        this.crmId = crmId;
    }

    public List<ProcessInfo> getCases() {
        return cases;
    }

    public void setCases(List<ProcessInfo> cases) {
        this.cases = cases;
        this.totalCases = cases != null ? cases.size() : 0;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }

    public static class ProcessInfo {
        private String processInstanceId;
        private String definitionKey;
        private String businessKey;
        private String state;
        private Date startTime;
        private Date endTime;

        public String getProcessInstanceId() {
            return processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
        }

        public String getDefinitionKey() {
            return definitionKey;
        }

        public void setDefinitionKey(String definitionKey) {
            this.definitionKey = definitionKey;
        }

        public String getBusinessKey() {
            return businessKey;
        }

        public void setBusinessKey(String businessKey) {
            this.businessKey = businessKey;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }
    }
}
