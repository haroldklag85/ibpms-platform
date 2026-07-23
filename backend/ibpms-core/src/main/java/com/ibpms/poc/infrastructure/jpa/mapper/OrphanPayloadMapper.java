// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.OrphanPayload;
import com.ibpms.poc.infrastructure.jpa.entity.OrphanPayloadJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrphanPayloadMapper {
    OrphanPayload toDomain(OrphanPayloadJpaEntity entity);
    OrphanPayloadJpaEntity toEntity(OrphanPayload domain);
}
