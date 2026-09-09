package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.IngestResult;
import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.LegalBaseSearchCriteria;
import com.tys.legalbases.application.dtos.PagedResponseDTO;
import com.tys.legalbases.application.mappers.NationalNormMapper;
import com.tys.legalbases.application.dtos.NationalNormArticleDTO;
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
import java.util.List;
import java.util.Optional;
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
     * falta. Nunca se reescribe la que ya lo tiene: el texto de una norma
     * vigente no se pisa desde una ingesta.
     */
    private IngestResult completarArticulado(NationalNormEntity existente, LegalBaseDTO data) {
        // La coleccion no se inicializa en la entidad: una norma cargada sin
        // articulos la trae en null, y sumarle ahi mismo explotaria.
        if (existente.getArticles() == null) {
            existente.setArticles(new ArrayList<>());
        }
        boolean sinArticulado = existente.getArticles().isEmpty();
        List<NationalNormArticleDTO> entrantes = Optional.ofNullable(data.articles()).orElseGet(List::of);

        if (!sinArticulado || entrantes.isEmpty()) {
            log.warn("National norm already exists, skipping: {} (articulado presente: {})",
                    data.title(), !sinArticulado);
            return IngestResult.alreadyExists();
        }

        // Se MUTA la coleccion que ya trae la entidad, no se reemplaza: sustituir
        // el PersistentBag de Hibernate por una lista inmutable de toList() hace
        // que el flush estalle con UnsupportedOperationException.
        entrantes.stream()
                .map(dto -> {
                    var article = nationalNormMapper.toArticleEntity(dto);
                    article.setNationalNorm(existente);
                    return article;
                })
                .forEach(existente.getArticles()::add);
        nationalNormRepository.save(existente);
        log.info("Articulado añadido a norma ya registrada: {} ({} artículos). "
                + "Ahora sí entra al vector store y el analizador puede citarla.",
                data.title(), entrantes.size());
        return IngestResult.articlesAdded();
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