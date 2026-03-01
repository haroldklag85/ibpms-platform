package com.ibpms.poc.application.dto;

/**
 * DTO para representar métricas de uso y ahorro del Copiloto IA.
 */
public class AiMetricsDTO {

    private long totalAiEvents; // Total de eventos registrados en AiAuditLog
    private long generatedDmns; // Reglas DMN autogeneradas
    private long autoRoutedEmails; // Emails enrutados por IA
    private double averageSimilarityScore; // Puntuación de similitud promedio (ej. Vectorial)

    public AiMetricsDTO() {
    }

    public long getTotalAiEvents() {
        return totalAiEvents;
    }

    public void setTotalAiEvents(long totalAiEvents) {
        this.totalAiEvents = totalAiEvents;
    }

    public long getGeneratedDmns() {
        return generatedDmns;
    }

    public void setGeneratedDmns(long generatedDmns) {
        this.generatedDmns = generatedDmns;
    }

    public long getAutoRoutedEmails() {
        return autoRoutedEmails;
    }

    public void setAutoRoutedEmails(long autoRoutedEmails) {
        this.autoRoutedEmails = autoRoutedEmails;
    }

    public double getAverageSimilarityScore() {
        return averageSimilarityScore;
    }

    public void setAverageSimilarityScore(double averageSimilarityScore) {
        this.averageSimilarityScore = averageSimilarityScore;
    }
}
