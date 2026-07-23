// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileProject;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileProjectJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgileProjectMapper {
    AgileProject toDomain(AgileProjectJpaEntity entity);
    AgileProjectJpaEntity toEntity(AgileProject domain);
}
