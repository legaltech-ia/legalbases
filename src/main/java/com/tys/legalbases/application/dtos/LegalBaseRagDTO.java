package com.tys.legalbases.application.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record LegalBaseRagDTO(
        Long id,
        String title,
        String description,
        String type,
        LocalDateTime publishedAt,
        String sourceUrl,
        List<NationalNormArticleDTO> articles
) {
}
