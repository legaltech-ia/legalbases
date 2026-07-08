package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.LegalBaseRagDTO;
import com.tys.legalbases.application.dtos.LegalBaseSaveDto;
import com.tys.legalbases.application.dtos.LegalBaseSearchCriteria;
import com.tys.legalbases.application.dtos.PagedResponseDTO;
import com.tys.legalbases.application.mappers.NationalNormMapper;
import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import com.tys.legalbases.domain.model.repository.NationalNormRepository;
import com.tys.legalbases.domain.model.repository.NationalNormSpecification;
import com.tys.legalbases.infrastructure.external.DatosGovCoFeignClient;
import com.tys.legalbases.infrastructure.external.dtos.DatosGovCoNormDTO;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalBaseService {

    private final NationalNormRepository nationalNormRepository;
    private final DatosGovCoFeignClient datosGovCoFeignClient;
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

    public void syncLegalBasesFromExternalSource() {
        List<DatosGovCoNormDTO> externalNorms = datosGovCoFeignClient.getLegalBases();

        externalNorms.forEach(register -> {
            NationalNormEntity newEntity = nationalNormMapper.toEntity(nationalNormMapper.toDto(register));
            Optional<NationalNormEntity> existingNorm = nationalNormRepository.findByTitle(newEntity.getTitle());

            if (existingNorm.isEmpty()) {
                nationalNormRepository.save(newEntity);
                log.debug("Saved new national norm: {}", newEntity.getTitle());
            } else {
                log.debug("National norm already exists, skipping: {}", newEntity.getTitle());
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<LegalBaseRagDTO> findLegalBaseById(Long id) {
        return nationalNormRepository.findByIdWithArticles(id)
                .map(nationalNormMapper::toRagDto);
    }

    public void save(LegalBaseDTO data, MultipartFile file) {
        var urlName = s3Service.uploadFile(file, Strings.EMPTY);
        var entity  = nationalNormMapper.toEntity(data);
        entity.setSourceUrl(urlName);
        this.nationalNormRepository.save(entity);
    }

    public void save(LegalBaseSaveDto data, MultipartFile file) {
        if (nationalNormRepository.findByTitle(data.title()).isPresent()) {
            log.warn("National norm already exists, skipping save: {}", data.title());
            return;
        }
        var entity = nationalNormMapper.toEntity(data);
        if (file != null && !file.isEmpty()) {
            var urlName = s3Service.uploadFile(file, Strings.EMPTY);
            entity.setSourceUrl(urlName);
        }
        if (data.articles() != null && !data.articles().isEmpty()) {
            var articles = data.articles().stream()
                    .map(nationalNormMapper::toArticleEntity)
                    .peek(article -> article.setNationalNorm(entity))
                    .toList();
            entity.setArticles(articles);
        }
        this.nationalNormRepository.save(entity);
    }

    public List<LegalBaseDTO> findAll() {
        return  nationalNormRepository.findAll()
                .stream()
                .map(nationalNormMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LegalBaseRagDTO> findAllForRag() {
        return nationalNormMapper.toRagDtoList(nationalNormRepository.findAllWithArticles());
    }
}