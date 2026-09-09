package com.tys.legalbases.application.mappers;

import com.tys.legalbases.domain.model.entity.CaseLawEntity;
import com.tys.legalbases.domain.model.entity.PrecedentRelation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live verification (curl contra el contenedor, 2026-08-20) found
 * sourceCaseLawId/targetCaseLawId always null in the API response: MapStruct
 * can't auto-match a *CaseLaw object field to a *CaseLawId Long target, and
 * ReportingPolicy.IGNORE let it through silently instead of failing the
 * build. This pins the fix.
 */
class CaseLawMapperTest {

    private final CaseLawMapper mapper = new CaseLawMapperImpl();

    @Test
    void precedentDtoCarriesBothCaseLawIds() {
        CaseLawEntity source = CaseLawEntity.builder().id(2L).filingNumber("C-500-2020").court("Corte Constitucional").build();
        CaseLawEntity target = CaseLawEntity.builder().id(1L).filingNumber("C-282-2017").court("Corte Constitucional").build();
        PrecedentRelation relation = PrecedentRelation.builder()
                .id(10L)
                .sourceCaseLaw(source)
                .targetCaseLaw(target)
                .relationType("REITERA")
                .context("misma ratio")
                .build();

        var dto = mapper.toPrecedentDto(relation);

        assertThat(dto.sourceCaseLawId()).isEqualTo(2L);
        assertThat(dto.targetCaseLawId()).isEqualTo(1L);
        assertThat(dto.sourceFilingNumber()).isEqualTo("C-500-2020");
        assertThat(dto.sourceCourt()).isEqualTo("Corte Constitucional");
    }
}
