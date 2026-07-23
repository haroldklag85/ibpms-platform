// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.TriageTask;
import com.ibpms.poc.infrastructure.jpa.entity.TriageTaskJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TriageTaskMapper {
    TriageTask toDomain(TriageTaskJpaEntity entity);
    TriageTaskJpaEntity toEntity(TriageTask domain);
}
