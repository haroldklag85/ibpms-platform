package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormDesignAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormDesignAuditLogRepository extends JpaRepository<FormDesignAuditLogEntity, Long> {
    List<FormDesignAuditLogEntity> findByFormIdOrderByTimestampDesc(UUID formId);
}
