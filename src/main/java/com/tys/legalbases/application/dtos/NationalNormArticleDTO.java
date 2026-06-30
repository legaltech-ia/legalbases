package com.tys.legalbases.application.dtos;

import java.time.LocalDateTime;

public record NationalNormArticleDTO(
        Long id,
        Integer articleNumber,
        String articleSuffix,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
