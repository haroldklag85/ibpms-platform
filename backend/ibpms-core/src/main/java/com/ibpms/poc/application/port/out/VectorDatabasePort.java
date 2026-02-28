package com.ibpms.poc.application.port.out;

import java.util.List;

/**
 * Capa Anti-Corrupción: Aísla la base de conocimiento de IA (Vectores)
 * de la implementación física (ej. PostgreSQL pgvector, Milvus, Qdrant).
 * Soporta MLOps y RAG (Retrieval-Augmented Generation).
 */
public interface VectorDatabasePort {

    /**
     * Guarda un vector que asocia un contexto (ej. correo original)
     * con la respuesta óptima validada por un humano (MLOps).
     */
    void saveFeedbackVector(String emailContext, String originalAiDraft, String humanFinalResponse,
            List<Double> embeddings);

    /**
     * Busca las 'topK' respuestas históricas más similares basándose en
     * los embeddings del contexto actual.
     */
    List<KnowledgeMatch> searchSimilarPastResponses(List<Double> currentContextEmbeddings, int topK);

    /**
     * DTO interno del puerto para no acoplar entidades de DB al dominio.
     */
    class KnowledgeMatch {
        private final String context;
        private final String humanApprovedReply;
        private final double distanceScore;

        public KnowledgeMatch(String context, String humanApprovedReply, double distanceScore) {
            this.context = context;
            this.humanApprovedReply = humanApprovedReply;
            this.distanceScore = distanceScore;
        }

        public String getContext() {
            return context;
        }

        public String getHumanApprovedReply() {
            return humanApprovedReply;
        }

        public double getDistanceScore() {
            return distanceScore;
        }
    }
}
