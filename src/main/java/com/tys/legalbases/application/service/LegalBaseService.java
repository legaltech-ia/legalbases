package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.IngestResult;
import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.domain.model.ArticleLabel;
import com.tys.legalbases.application.dtos.LegalBaseSearchCriteria;
import com.tys.legalbases.application.dtos.PagedResponseDTO;
import com.tys.legalbases.application.mappers.NationalNormMapper;
import com.tys.legalbases.application.dtos.NationalNormArticleDTO;
import com.tys.legalbases.domain.model.entity.ArticleEntity;
import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import com.tys.legalbases.domain.model.repository.NationalNormRepository;
import com.tys.legalbases.domain.model.repository.NationalNormSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalBaseService {

    private final NationalNormRepository nationalNormRepository;
    private final NationalNormMapper nationalNormMapper;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public PagedResponseDTO<LegalBaseDTO> findAllLegalBases(LegalBaseSearchCriteria criteria) {
        Sort sort = criteria.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(criteria.getSortBy()).ascending()
                : Sort.by(criteria.getSortBy()).descending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        Page<NationalNormEntity> resultPage = nationalNormRepository.findAll(
                NationalNormSpecification.searchAllFields(criteria.getSearch()), pageable
        );

        List<LegalBaseDTO> content = nationalNormMapper.toDtoList(resultPage.getContent());

        return new PagedResponseDTO<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public Optional<LegalBaseDTO> findLegalBaseById(Long id) {
        return nationalNormRepository.findByIdWithArticles(id)
                .map(nationalNormMapper::toDetailDto);
    }

    public IngestResult save(LegalBaseDTO data, MultipartFile file) {
        // Upload only after the duplicate check inside ingest, via the enrich callback.
        return ingest(data, entity -> Optional.ofNullable(file)
                .filter(f -> !f.isEmpty())
                .map(f -> s3Service.uploadFile(f, Strings.EMPTY))
                .ifPresent(entity::setSourceUrl));
    }

    public List<LegalBaseDTO> findAll() {
        return nationalNormRepository.findAll()
                .stream()
                .map(nationalNormMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LegalBaseDTO> findAllForRag() {
        return nationalNormMapper.toDetailDtoList(nationalNormRepository.findAllWithArticles());
    }

    public IngestResult ingestFromDto(LegalBaseDTO data) {
        return ingest(data, entity -> { });
    }

    private IngestResult ingest(LegalBaseDTO data, Consumer<NationalNormEntity> enrich) {
        Optional<NationalNormEntity> existente = nationalNormRepository.findByTitle(data.title());
        if (existente.isPresent()) {
            return completarArticulado(existente.get(), data);
        }
        var entity = buildEntity(data);
        enrich.accept(entity);
        nationalNormRepository.save(entity);
        log.debug("Ingested national norm: {}", data.title());
        return IngestResult.created();
    }

    /**
     * Una norma ya registrada solo se toca para ponerle el articulado que le
     * falta, articulo por articulo. Nunca se reescribe el que ya esta: el texto
     * de una norma vigente no se pisa desde una ingesta.
     *
     * <p>La guarda era por NORMA --"ya tiene articulado"-- y eso hacia
     * imposible completar un articulado parcial, que es justo el caso que
     * importa: la Ley 1801 entro con 193 de sus 243 articulos y los 50 que
     * faltaban --136 a 160 y 186 a 210, con las atribuciones de las autoridades
     * de policia dentro-- no habia forma de meterlos sin tocar la base a mano.
     */
    private IngestResult completarArticulado(NationalNormEntity existente, LegalBaseDTO data) {
        // La coleccion no se inicializa en la entidad: una norma cargada sin
        // articulos la trae en null, y sumarle ahi mismo explotaria.
        if (existente.getArticles() == null) {
            existente.setArticles(new ArrayList<>());
        }
        Set<String> yaEscritos = existente.getArticles().stream()
                .map(LegalBaseService::designacionDe)
                .collect(Collectors.toSet());

        // Dos pasadas de la misma regla: el payload no repite un articulo
        // consigo mismo --50 de los 243 de la Ley 1801 entraron duplicados byte
        // a byte, y cada repetido se come un hueco del topK del RAG, que
        // recupera tres-- y tampoco repite uno que la norma ya tiene escrito.
        List<NationalNormArticleDTO> entrantes = Optional.ofNullable(data.articles()).orElseGet(List::of);
        List<NationalNormArticleDTO> nuevos = entrantes.stream()
                .filter(dto -> Objects.nonNull(dto) && Objects.nonNull(dto.content()))
                .collect(Collectors.toMap(
                        LegalBaseService::designacionDe, dto -> dto, (primero, ignorado) -> primero,
                        LinkedHashMap::new))
                .entrySet().stream()
                .filter(entrada -> !yaEscritos.contains(entrada.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        if (nuevos.isEmpty()) {
            log.warn("Ingesta de '{}': ninguno de los {} articulo(s) del payload aporta algo nuevo, no se toca nada",
                    data.title(), entrantes.size());
            return IngestResult.alreadyExists();
        }

        Optional.of(entrantes.size() - nuevos.size())
                .filter(descartados -> descartados > 0)
                .ifPresent(descartados -> log.warn(
                        "Ingesta de '{}': {} articulo(s) descartados por repetidos o ya presentes",
                        data.title(), descartados));

        // Se MUTA la coleccion que ya trae la entidad, no se reemplaza: sustituir
        // el PersistentBag de Hibernate por una lista inmutable de toList() hace
        // que el flush estalle con UnsupportedOperationException.
        nuevos.stream()
                .map(dto -> {
                    var article = nationalNormMapper.toArticleEntity(dto);
                    article.setNationalNorm(existente);
                    return article;
                })
                .forEach(existente.getArticles()::add);
        nationalNormRepository.save(existente);
        log.info("Articulado completado en norma ya registrada: {} (+{} articulos, {} en total). "
                + "Ahora si entran al vector store y el analizador puede citarlos.",
                data.title(), nuevos.size(), existente.getArticles().size());
        return IngestResult.articlesAdded();
    }

    private static String designacionDe(ArticleEntity entity) {
        return Optional.ofNullable(entity.getArticleLabel())
                .filter(label -> !label.isBlank())
                .orElseGet(() -> ArticleLabel.fromLegacy(entity.getArticleNumber(), entity.getArticleSuffix()));
    }

    /** Dos articulos con la misma designacion son el mismo articulo. */
    private static String designacionDe(NationalNormArticleDTO dto) {
        return Optional.ofNullable(dto.articleLabel())
                .filter(label -> !label.isBlank())
                .orElseGet(() -> ArticleLabel.fromLegacy(dto.articleNumber(), dto.articleSuffix()));
    }

    private NationalNormEntity buildEntity(LegalBaseDTO data) {
        var entity = nationalNormMapper.toEntity(data);
        Optional.ofNullable(data.articles())
                .filter(articles -> !articles.isEmpty())
                .map(articles -> articles.stream()
                        .map(dto -> {
                            var article = nationalNormMapper.toArticleEntity(dto);
                            article.setNationalNorm(entity);
                            return article;
                        })
                        .toList())
                .ifPresent(entity::setArticles);
        return entity;
    }
}