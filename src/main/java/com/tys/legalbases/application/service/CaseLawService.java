package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.mappers.CaseLawMapper;
import com.tys.legalbases.domain.exception.ResourceNotFoundException;
import com.tys.legalbases.domain.model.entity.CaseLawEntity;
import com.tys.legalbases.domain.model.entity.PrecedentRelation;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseLawService {

    private final CaseLawRepository caseLawRepository;
    private final CaseLawMapper caseLawMapper;

    @Transactional(readOnly = true)
    public List<CaseLawDTO> findAll() {
        return caseLawMapper.toDtoList(caseLawRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<CaseLawDTO> findById(Long id) {
        return caseLawRepository.findById(id)
                .map(caseLawMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<CaseLawDTO> findByFilingNumber(String filingNumber) {
        return caseLawRepository.findByFilingNumber(filingNumber)
                .map(caseLawMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<CaseLawDTO> findByTopic(String topic) {
        return caseLawMapper.toDtoList(caseLawRepository.findByTopic(topic));
    }

    @Transactional(readOnly = true)
    public List<CaseLawDTO> findByCourt(String court) {
        return caseLawMapper.toDtoList(caseLawRepository.findByCourt(court));
    }

    @Transactional(readOnly = true)
    public List<CaseLawDTO> findByCourtAndTopic(String court, String topic) {
        return caseLawMapper.toDtoList(caseLawRepository.findByCourtAndTopic(court, topic));
    }

    @Transactional(readOnly = true)
    public List<CaseLawDTO> findByDateBetween(LocalDateTime from, LocalDateTime to) {
        return caseLawMapper.toDtoList(caseLawRepository.findByDateBetween(from, to));
    }

    @Transactional(readOnly = true)
    public List<CaseLawDTO> fullTextSearch(String query) {
        return caseLawMapper.toDtoList(caseLawRepository.fullTextSearch(query));
    }

    @Transactional
    public CaseLawDTO save(CaseLawDTO dto) {
        var entity = caseLawMapper.toEntity(dto);
        var saved = caseLawRepository.save(entity);
        log.debug("Saved case law: {} - {}", saved.getCourt(), saved.getFilingNumber());
        return caseLawMapper.toDto(saved);
    }

    @Transactional
    public void saveAll(List<CaseLawDTO> dtos) {
        var entities = caseLawMapper.toEntityList(dtos);
        caseLawRepository.saveAll(entities);
        log.debug("Batch saved {} case law records", dtos.size());
    }

    @Transactional
    public void addPrecedentRelation(Long sourceId, Long targetId, String relationType, String context) {
        var source = caseLawRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source case law not found: " + sourceId));
        var target = caseLawRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Target case law not found: " + targetId));

        var relation = PrecedentRelation.builder()
                .sourceCaseLaw(source)
                .targetCaseLaw(target)
                .relationType(relationType)
                .context(context)
                .build();

        source.getOutgoingPrecedents().add(relation);
        caseLawRepository.save(source);
        log.debug("Added precedent relation: {} -> {} ({})", source.getFilingNumber(), target.getFilingNumber(), relationType);
    }

    @Transactional
    public void deleteById(Long id) {
        caseLawRepository.deleteById(id);
        log.debug("Deleted case law: {}", id);
    }
}
