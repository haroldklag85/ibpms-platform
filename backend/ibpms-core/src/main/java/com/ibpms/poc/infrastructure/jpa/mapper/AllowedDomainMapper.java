// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.AllowedDomain;
import com.ibpms.poc.infrastructure.jpa.entity.AllowedDomainJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllowedDomainMapper {
    AllowedDomain toDomain(AllowedDomainJpaEntity entity);
    AllowedDomainJpaEntity toEntity(AllowedDomain domain);
}
