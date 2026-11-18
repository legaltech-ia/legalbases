package com.tys.legalbases.application.dtos;

import java.time.LocalDateTime;

/**
 * @param articleLabel  la designación tal como la publica la norma ("27",
 *                      "223A", "2.2.8.18.7.1"). Es lo que hay que leer.
 * @param articleNumber proyección legada: ordena, no identifica.
 * @param articleSuffix proyección legada; ver {@code articleNumber}.
 */
public record NationalNormArticleDTO(
        Long id,
        String articleLabel,
        Integer articleNumber,
        String articleSuffix,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
