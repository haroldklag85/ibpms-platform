package com.ibpms.poc.infrastructure.jpa.adapter;

import com.ibpms.poc.application.port.out.VectorDatabasePort;
import com.ibpms.poc.infrastructure.jpa.entity.AiKnowledgeVectorEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ai.AiKnowledgeVectorRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapeador e implementación de Capa Anti-Corrupción para Vector DB (RAG).
 * Se comunica con PostgreSQL mediante la extensión pgvector.
 */
@Component
public class PgVectorAdapter implements VectorDatabasePort {

    private final AiKnowledgeVectorRepository repository;

    public PgVectorAdapter(AiKnowledgeVectorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveFeedbackVector(String emailContext, String originalAiDraft, String humanFinalResponse,
            List<Double> embeddings) {
        AiKnowledgeVectorEntity entity = new AiKnowledgeVectorEntity();
        entity.setContextEmailBody(emailContext);
        entity.setHumanApprovedReply(humanFinalResponse);

        // Conversión limpia de List<Double> (API de Dominio/LLM) a float array
        // (pgvector JDBC type)
        float[] floatArray = new float[embeddings.size()];
        for (int i = 0; i < embeddings.size(); i++) {
            floatArray[i] = embeddings.get(i).floatValue();
        }
        entity.setEmbedding(floatArray);

        repository.save(entity);
    }

    @Override
    public List<KnowledgeMatch> searchSimilarPastResponses(List<Double> currentContextEmbeddings, int topK) {
        // Para pasar el array de floats como string formatedo a Native SQL ("[...]"):
        StringBuilder vectorString = new StringBuilder("[");
        for (int i = 0; i < currentContextEmbeddings.size(); i++) {
            vectorString.append(currentContextEmbeddings.get(i));
            if (i < currentContextEmbeddings.size() - 1) {
                vectorString.append(",");
            }
        }
        vectorString.append("]");

        List<AiKnowledgeVectorEntity> nearest = repository.findNearestNeighbors(vectorString.toString(), topK);

        return nearest.stream()
                .map(e -> new KnowledgeMatch(
                        e.getContextEmailBody(),
                        e.getHumanApprovedReply(),
                        0.0 // En PostgreSQL el score exacto (cosine) se puede obtener alterando la query,
                            // pero la ordenación ya es the "mejor a peor"
                ))
                .collect(Collectors.toList());
    }
}
