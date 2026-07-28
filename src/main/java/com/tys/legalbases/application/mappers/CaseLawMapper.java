package com.tys.legalbases.application.mappers;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.dtos.PrecedentDTO;
import com.tys.legalbases.domain.model.entity.CaseLawEntity;
import com.tys.legalbases.domain.model.entity.PrecedentRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CaseLawMapper {

    @Mapping(target = "precedents", source = "outgoingPrecedents")
    CaseLawDTO toDto(CaseLawEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "outgoingPrecedents", ignore = true)
    @Mapping(target = "incomingPrecedents", ignore = true)
    CaseLawEntity toEntity(CaseLawDTO dto);

    @Mapping(target = "sourceFilingNumber", source = "sourceCaseLaw.filingNumber")
    @Mapping(target = "sourceCourt", source = "sourceCaseLaw.court")
    PrecedentDTO toPrecedentDto(PrecedentRelation relation);

    List<CaseLawDTO> toDtoList(List<CaseLawEntity> entities);
    List<CaseLawEntity> toEntityList(List<CaseLawDTO> dtos);
}
