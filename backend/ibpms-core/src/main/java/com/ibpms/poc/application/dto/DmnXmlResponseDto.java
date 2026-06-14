package com.ibpms.poc.application.dto;

public record DmnXmlResponseDto(
        String xmlContent,
        double confidenceScore,
        String reasoningTrace
) {}
