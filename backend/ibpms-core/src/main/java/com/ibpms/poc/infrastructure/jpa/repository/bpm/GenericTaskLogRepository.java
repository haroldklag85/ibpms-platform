package com.ibpms.poc.infrastructure.jpa.repository.bpm;

import com.ibpms.poc.infrastructure.jpa.entity.bpm.GenericTaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenericTaskLogRepository extends JpaRepository<GenericTaskLogEntity, UUID> {
}
