package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.ExternalTaskTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalTaskTopicRepository extends JpaRepository<ExternalTaskTopicEntity, String> {
    List<ExternalTaskTopicEntity> findByIsActiveTrue();
}
