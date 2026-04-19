package com.ibpms.poc.application.dto;

public record DmnDefinitionDto(
    String id,
    String key,
    String name,
    int version,
    String deploymentId,
    String deploymentDate
) {}
