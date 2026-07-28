package com.tys.legalbases.application.mappers;

import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class NationalNormMapperTest {

    private final NationalNormMapper mapper = Mappers.getMapper(NationalNormMapper.class);

    @Test
    void mapsAllColombianNormFields() {
        NationalNormEntity entity = new NationalNormEntity();
        entity.setTitle("Ley 1801 de 2016");
        entity.setDescription("Codigo Nacional de Seguridad y Convivencia Ciudadana");
        entity.setType("LEY");
        entity.setSourceUrl("https://www.suin-juriscol.gov.co/");
        entity.setTipo("Ley");
        entity.setNumero("1801");
        entity.setAnio(2016);
        entity.setSector("Interior");
        entity.setSubtipo("Ordinaria");
        entity.setVigencia("vigente");
        entity.setEntidad("Congreso de la Republica");
        entity.setMateria("Convivencia ciudadana");
        entity.setArticulos("243");

        LegalBaseDTO dto = mapper.toDto(entity);

        assertThat(dto.tipo()).isEqualTo("Ley");
        assertThat(dto.numero()).isEqualTo("1801");
        assertThat(dto.anio()).isEqualTo(2016);
        assertThat(dto.sector()).isEqualTo("Interior");
        assertThat(dto.subtipo()).isEqualTo("Ordinaria");
        assertThat(dto.vigencia()).isEqualTo("vigente");
        assertThat(dto.entidad()).isEqualTo("Congreso de la Republica");
        assertThat(dto.materia()).isEqualTo("Convivencia ciudadana");
        assertThat(dto.articulos()).isEqualTo("243");
    }
}
