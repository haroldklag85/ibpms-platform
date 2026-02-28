package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.AiKnowledgeVectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiKnowledgeVectorRepository extends JpaRepository<AiKnowledgeVectorEntity, UUID> {

    // Utilizando operador <-> para distancia de Coseno en pgvector
    @Query(value = "SELECT * FROM ai_knowledge_vectors ORDER BY embedding <-> cast(?1 as vector) LIMIT ?2", nativeQuery = true)
    List<AiKnowledgeVectorEntity> findNearestNeighbors(String vectorString, int limit);
}
