package com.tys.legalbases.application.dtos;

public record PrecedentDTO(
    Long id,
    Long sourceCaseLawId,
    Long targetCaseLawId,
    String relationType,
    String context,
    String sourceFilingNumber,
    String sourceCourt
) {}
