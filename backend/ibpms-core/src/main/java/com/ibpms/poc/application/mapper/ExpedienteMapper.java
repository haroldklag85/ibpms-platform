package com.ibpms.poc.application.mapper;

import com.ibpms.poc.application.dto.ExpedienteDTO;
import com.ibpms.poc.domain.model.Expediente;
import com.ibpms.poc.infrastructure.jpa.entity.ExpedienteEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface ExpedienteMapper {
    ExpedienteMapper INSTANCE = Mappers.getMapper(ExpedienteMapper.class);

    // Domain -> DTO (for response)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    ExpedienteDTO toDto(Expediente domain);

    // DTO -> Domain (for creation)
    default Expediente toDomain(ExpedienteDTO dto) {
        if (dto == null) {
            return null;
        }
        return Expediente.iniciarNuevo(
                dto.getDefinitionKey(),
                dto.getBusinessKey(),
                dto.getType(),
                dto.getVariables());
    }

    // Entity -> Domain
    default Expediente toDomain(ExpedienteEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Expediente.Builder()
                .id(entity.getId() != null ? java.util.UUID.fromString(entity.getId()) : null)
                .definitionKey(entity.getDefinitionKey())
                .businessKey(entity.getBusinessKey())
                .type(entity.getType())
                .status(entity.getStatus() != null ? Expediente.ExpedienteStatus.valueOf(entity.getStatus()) : null)
                .variables(map(entity.getPayload()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // Domain -> Entity (for persistence)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "definitionKey", source = "definitionKey")
    @Mapping(target = "businessKey", source = "businessKey")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "payload", source = "variables")
    @Mapping(target = "createdAt", source = "createdAt")
    ExpedienteEntity toEntity(Expediente domain);

    default String map(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error mapping variables map to JSON string", e);
        }
    }

    default Map<String, Object> map(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new ObjectMapper().readValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error mapping JSON string to variables map", e);
        }
    }
}
