package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.TaskSkipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface TaskSkipRepository extends JpaRepository<TaskSkipEntity, UUID> {
    
    @Query(value = "SELECT COUNT(*) FROM ibpms_task_skips s WHERE s.user_id = :userId AND s.tenant_id = :tenantId AND s.created_at >= :since", nativeQuery = true)
    int countRecentSkips(@Param("tenantId") String tenantId, @Param("userId") String userId, @Param("since") LocalDateTime since);
}
