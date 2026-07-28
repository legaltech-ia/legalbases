package com.tys.legalbases.application.mappers;

import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.NationalNormArticleDTO;
import com.tys.legalbases.domain.model.entity.ArticleEntity;
import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NationalNormMapper {

    NationalNormMapper INSTANCE = Mappers.getMapper(NationalNormMapper.class);

    @Named("toDto")
    @Mapping(target = "articles", ignore = true)
    LegalBaseDTO toDto(NationalNormEntity entity);

    @Named("toDetailDto")
    LegalBaseDTO toDetailDto(NationalNormEntity entity);

    NationalNormArticleDTO toArticleDto(ArticleEntity entity);

    @Mapping(target = "articles", ignore = true)
    NationalNormEntity toEntity(LegalBaseDTO dto);

    @Mapping(target = "nationalNorm", ignore = true)
    ArticleEntity toArticleEntity(NationalNormArticleDTO dto);

    @IterableMapping(qualifiedByName = "toDto")
    List<LegalBaseDTO> toDtoList(List<NationalNormEntity> entities);

    @IterableMapping(qualifiedByName = "toDetailDto")
    List<LegalBaseDTO> toDetailDtoList(List<NationalNormEntity> entities);

    List<NationalNormEntity> toEntityList(List<LegalBaseDTO> dtos);
}
