package com.ibpms.poc.application.dto;

/**
 * DTO para representar el estado de salud y eficacia de los procesos (BAM).
 */
public class ProcessHealthDTO {

    private long activeCases;
    private long completedCases;
    private long activeTasks;
    private long overdueTasks; // Tareas que pasaron su SLA / Due Date

    public ProcessHealthDTO() {
    }

    public ProcessHealthDTO(long activeCases, long completedCases, long activeTasks, long overdueTasks) {
        this.activeCases = activeCases;
        this.completedCases = completedCases;
        this.activeTasks = activeTasks;
        this.overdueTasks = overdueTasks;
    }

    public long getActiveCases() {
        return activeCases;
    }

    public void setActiveCases(long activeCases) {
        this.activeCases = activeCases;
    }

    public long getCompletedCases() {
        return completedCases;
    }

    public void setCompletedCases(long completedCases) {
        this.completedCases = completedCases;
    }

    public long getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(long activeTasks) {
        this.activeTasks = activeTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }
}
