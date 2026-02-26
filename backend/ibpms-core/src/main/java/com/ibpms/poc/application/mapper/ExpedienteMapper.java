package com.ibpms.poc.application.mapper;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.domain.model.Expediente;
import com.ibpms.poc.infrastructure.jpa.entity.ExpedienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExpedienteMapper {
    ExpedienteMapper INSTANCE = Mappers.getMapper(ExpedienteMapper.class);

    // Domain -> DTO (for response)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    ExpedienteDTO toDto(Expediente domain);

    // DTO -> Domain (for creation)
    @Mapping(target = "id", ignore = true) // will be generated in domain
    @Mapping(target = "status", ignore = true) // set to ACTIVE in domain
    @Mapping(target = "createdAt", ignore = true)
    Expediente toDomain(ExpedienteDTO dto);

    // Entity -> Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "definitionKey", source = "definitionKey")
    @Mapping(target = "businessKey", source = "businessKey")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "variables", source = "payload")
    @Mapping(target = "createdAt", source = "createdAt")
    Expediente toDomain(ExpedienteEntity entity);

    // Domain -> Entity (for persistence)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "definitionKey", source = "definitionKey")
    @Mapping(target = "businessKey", source = "businessKey")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "payload", source = "variables")
    @Mapping(target = "createdAt", source = "createdAt")
    ExpedienteEntity toEntity(Expediente domain);
}
