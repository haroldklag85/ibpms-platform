package com.ibpms.poc.infrastructure.jpa.repository.ai;

import com.ibpms.poc.infrastructure.jpa.entity.AiKnowledgeVectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiKnowledgeVectorRepository extends JpaRepository<AiKnowledgeVectorEntity, UUID> {

    // Búsqueda Vectorial Real (KNN - L2 Distance) usando pgvector nativo
    @Query(value = "SELECT * FROM ai_knowledge_vectors ORDER BY embedding <-> cast(:vector as vector) LIMIT :limit", nativeQuery = true)
    List<AiKnowledgeVectorEntity> findNearestNeighbors(@Param("vector") String vector, @Param("limit") int limit);
}
