package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.mappers.CaseLawMapper;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaseLawQueryService {

    private final CaseLawRepository caseLawRepository;
    private final CaseLawMapper caseLawMapper;

    public List<CaseLawDTO> findAll() {
        return caseLawMapper.toDtoList(caseLawRepository.findAll());
    }

    public Optional<CaseLawDTO> findById(Long id) {
        return caseLawRepository.findById(id)
                .map(caseLawMapper::toDto);
    }

    public Optional<CaseLawDTO> findByFilingNumber(String filingNumber) {
        return caseLawRepository.findByFilingNumber(filingNumber)
                .map(caseLawMapper::toDto);
    }

    public List<CaseLawDTO> findByTopic(String topic) {
        return caseLawMapper.toDtoList(caseLawRepository.findByTopic(topic));
    }

    public List<CaseLawDTO> findByCourt(String court) {
        return caseLawMapper.toDtoList(caseLawRepository.findByCourt(court));
    }

    public List<CaseLawDTO> findByCourtAndTopic(String court, String topic) {
        return caseLawMapper.toDtoList(caseLawRepository.findByCourtAndTopic(court, topic));
    }

    public List<CaseLawDTO> findByDateBetween(LocalDateTime from, LocalDateTime to) {
        return caseLawMapper.toDtoList(caseLawRepository.findByDateBetween(from, to));
    }

    public List<CaseLawDTO> fullTextSearch(String query) {
        return caseLawMapper.toDtoList(caseLawRepository.fullTextSearch(query));
    }
}
