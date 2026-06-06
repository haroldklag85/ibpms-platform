package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KanbanColumnRepository extends JpaRepository<KanbanColumnEntity, UUID> {
    List<KanbanColumnEntity> findByBoardId(UUID boardId);
    long countByBoardId(UUID boardId);
}
