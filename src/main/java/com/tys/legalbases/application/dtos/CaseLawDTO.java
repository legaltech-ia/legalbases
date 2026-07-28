package com.tys.legalbases.application.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record CaseLawDTO(
    Long id,
    String court,
    String chamber,
    String filingNumber,
    LocalDateTime date,
    String rapporteur,
    String mainTopic,
    String decisionType,
    String fullText,
    String summary,
    String sourceUrl,
    List<PrecedentDTO> precedents
) {}
