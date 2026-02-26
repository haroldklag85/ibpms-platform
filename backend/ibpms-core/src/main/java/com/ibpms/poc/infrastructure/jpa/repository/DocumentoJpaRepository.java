package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.DocumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoJpaRepository extends JpaRepository<DocumentoEntity, String> {
    List<DocumentoEntity> findByCaseIdOrderByUploadedAtDesc(String caseId);
}
