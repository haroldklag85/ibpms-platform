// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.jpa.mapper;

import com.ibpms.poc.domain.model.WebhookTransaction;
import com.ibpms.poc.infrastructure.jpa.entity.WebhookTransactionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebhookTransactionMapper {
    WebhookTransaction toDomain(WebhookTransactionJpaEntity entity);
    WebhookTransactionJpaEntity toEntity(WebhookTransaction domain);
}
