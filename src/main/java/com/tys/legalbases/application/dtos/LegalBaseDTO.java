package com.tys.legalbases.application.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record LegalBaseDTO(
    Long id,
    String title,
    String description,
    String type,
    LocalDateTime publishedAt,
    String sourceUrl,
    List<NationalNormArticleDTO> articles,
    String tipo,
    String numero,
    Integer anio,
    String sector,
    String subtipo,
    String vigencia,
    String entidad,
    String materia,
    String articulos
) {}
