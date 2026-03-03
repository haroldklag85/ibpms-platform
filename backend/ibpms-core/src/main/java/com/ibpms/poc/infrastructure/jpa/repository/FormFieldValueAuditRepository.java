package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.FormFieldValueAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormFieldValueAuditRepository extends JpaRepository<FormFieldValueAuditEntity, UUID> {

    List<FormFieldValueAuditEntity> findByProcessInstanceIdOrderByChangedAtDesc(String processInstanceId);
}
