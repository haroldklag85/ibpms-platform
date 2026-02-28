package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KanbanTaskRepository extends JpaRepository<KanbanTaskEntity, UUID> {
    List<KanbanTaskEntity> findByBoardIdOrderByUpdatedAtDesc(UUID boardId);
}
