// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTaskJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgileTaskMapper {
    AgileTask toDomain(AgileTaskJpaEntity entity);
    AgileTaskJpaEntity toEntity(AgileTask domain);
}
