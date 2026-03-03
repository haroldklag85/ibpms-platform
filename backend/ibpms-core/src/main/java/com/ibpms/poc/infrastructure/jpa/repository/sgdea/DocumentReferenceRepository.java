package com.ibpms.poc.infrastructure.jpa.repository.sgdea;

import com.ibpms.poc.infrastructure.jpa.entity.sgdea.DocumentReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentReferenceRepository extends JpaRepository<DocumentReferenceEntity, UUID> {

    List<DocumentReferenceEntity> findByProcessInstanceIdOrderByCreatedAtDesc(String processInstanceId);

    Optional<DocumentReferenceEntity> findBySharepointGraphId(String sharepointGraphId);
}
