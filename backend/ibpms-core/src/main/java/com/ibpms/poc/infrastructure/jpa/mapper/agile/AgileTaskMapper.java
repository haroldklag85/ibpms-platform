// @Traceability: US-005, CA-42 - Activity Timeline
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileTaskJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgileTaskMapper {
    AgileTask toDomain(AgileTaskJpaEntity entity);
    AgileTaskJpaEntity toEntity(AgileTask domain);

    default java.util.UUID map(String value) {
        return value != null ? java.util.UUID.fromString(value) : null;
    }
    default String map(java.util.UUID value) {
        return value != null ? value.toString() : null;
    }
}
