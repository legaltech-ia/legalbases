package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.IngestResult;
import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.NationalNormArticleDTO;
import com.tys.legalbases.application.mappers.NationalNormMapper;
import com.tys.legalbases.domain.model.entity.ArticleEntity;
import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import com.tys.legalbases.domain.model.repository.NationalNormRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Medido el 19 ago 2026: de las 89.366 normas del snapshot de SUIN-Juriscol,
 * UNA tiene articulado (Ley 1801). El Decreto 768 de 2025, la Ley 675 de 2001 y
 * la Ley 2197 de 2022 estan registradas como titulo con CERO articulos, y una
 * norma sin articulado no se vectoriza ni se puede citar.
 *
 * La ingesta cortaba antes de llegar ahi: `alreadyExists(title)` devolvia
 * ALREADY_EXISTS para cualquier norma ya registrada, asi que completar el
 * articulado que falta era imposible — un no-op silencioso justo en el caso que
 * importa. Estos tests fijan el arreglo.
 */
@ExtendWith(MockitoExtension.class)
class LegalBaseServiceIngestTest {

    private static final String TITULO = "DECRETO 768 DE 2025";

    @Mock private NationalNormRepository nationalNormRepository;
    @Mock private NationalNormMapper nationalNormMapper;
    @Mock private S3Service s3Service;

    @InjectMocks private LegalBaseService legalBaseService;

    private static NationalNormArticleDTO articulo(int numero, String contenido) {
        return new NationalNormArticleDTO(null, numero, null, "Titulo " + numero, contenido, null);
    }

    private static LegalBaseDTO conArticulado(NationalNormArticleDTO... articulos) {
        return new LegalBaseDTO(null, TITULO, null, "Decreto", null, null, List.of(articulos),
                null, null, null, null, null, null, null, null, null);
    }

    private static NationalNormEntity registradaSinArticulado() {
        var entidad = new NationalNormEntity();
        entidad.setTitle(TITULO);
        entidad.setArticles(new ArrayList<>());
        return entidad;
    }

    private void mapeaLosArticulos() {
        lenient().when(nationalNormMapper.toArticleEntity(any()))
                .thenAnswer(inv -> {
                    NationalNormArticleDTO dto = inv.getArgument(0);
                    var e = new ArticleEntity();
                    e.setArticleNumber(dto.articleNumber());
                    e.setContent(dto.content());
                    return e;
                });
    }

    @Test
    void aNormaRegistradaSinArticuladoSeLePoneElTexto() {
        when(nationalNormRepository.findByTitle(TITULO)).thenReturn(Optional.of(registradaSinArticulado()));
        mapeaLosArticulos();

        IngestResult resultado = legalBaseService.ingestFromDto(
                conArticulado(articulo(1, "Ambito de aplicacion..."), articulo(2, "Autonomia...")));

        assertThat(resultado.outcome()).isEqualTo(IngestResult.Outcome.ARTICLES_ADDED);

        ArgumentCaptor<NationalNormEntity> guardada = ArgumentCaptor.forClass(NationalNormEntity.class);
        verify(nationalNormRepository).save(guardada.capture());
        assertThat(guardada.getValue().getArticles()).hasSize(2);
    }

    /** El texto de una norma vigente no se pisa desde una ingesta. */
    @Test
    void unaNormaQueYaTieneArticuladoNoSeSobrescribe() {
        var conTexto = registradaSinArticulado();
        var yaExistente = new ArticleEntity();
        yaExistente.setArticleNumber(1);
        yaExistente.setContent("El texto bueno, el que ya estaba");
        conTexto.setArticles(new ArrayList<>(List.of(yaExistente)));
        when(nationalNormRepository.findByTitle(TITULO)).thenReturn(Optional.of(conTexto));

        IngestResult resultado = legalBaseService.ingestFromDto(conArticulado(articulo(1, "Otro texto")));

        assertThat(resultado.outcome()).isEqualTo(IngestResult.Outcome.ALREADY_EXISTS);
        verify(nationalNormRepository, never()).save(any());
    }

    /** Reingerir el mismo titulo sin articulos sigue siendo un no-op idempotente. */
    @Test
    void sinArticulosQueAportarNoSeTocaNada() {
        when(nationalNormRepository.findByTitle(TITULO)).thenReturn(Optional.of(registradaSinArticulado()));

        IngestResult resultado = legalBaseService.ingestFromDto(conArticulado());

        assertThat(resultado.outcome()).isEqualTo(IngestResult.Outcome.ALREADY_EXISTS);
        verify(nationalNormRepository, never()).save(any());
    }

    @Test
    void unaNormaNuevaSeSigueCreando() {
        when(nationalNormRepository.findByTitle(TITULO)).thenReturn(Optional.empty());
        when(nationalNormMapper.toEntity(any())).thenReturn(new NationalNormEntity());
        mapeaLosArticulos();

        IngestResult resultado = legalBaseService.ingestFromDto(conArticulado(articulo(1, "texto")));

        assertThat(resultado.outcome()).isEqualTo(IngestResult.Outcome.CREATED);
        verify(nationalNormRepository).save(any());
    }
}
