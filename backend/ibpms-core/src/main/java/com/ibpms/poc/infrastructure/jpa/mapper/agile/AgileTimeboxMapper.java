// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileTimebox;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTimeboxJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgileTimeboxMapper {
    AgileTimebox toDomain(AgileTimeboxJpaEntity entity);
    AgileTimeboxJpaEntity toEntity(AgileTimebox domain);
}
