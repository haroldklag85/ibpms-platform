// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.DmnModel;
import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DmnModelMapper {
    DmnModel toDomain(DmnModelJpaEntity entity);
    DmnModelJpaEntity toEntity(DmnModel domain);
}
