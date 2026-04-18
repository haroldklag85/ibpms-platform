package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IbpmsRoleRepository extends JpaRepository<IbpmsRoleEntity, UUID> {
    
    // CA-6: Query for VIP restricted roles
    List<IbpmsRoleEntity> findByIsVipRestrictedTrue();
}
