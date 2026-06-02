// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper.agile;

import com.ibpms.poc.domain.model.agile.AgileSlaChangelog;
import com.ibpms.poc.infrastructure.jpa.entity.agile.AgileSlaChangelogJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgileSlaChangelogMapper {
    AgileSlaChangelog toDomain(AgileSlaChangelogJpaEntity entity);
    AgileSlaChangelogJpaEntity toEntity(AgileSlaChangelog domain);
}
