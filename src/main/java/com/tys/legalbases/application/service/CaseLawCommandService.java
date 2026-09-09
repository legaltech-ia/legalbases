package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.mappers.CaseLawMapper;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CaseLawCommandService {

    private final CaseLawRepository caseLawRepository;
    private final CaseLawMapper caseLawMapper;

    public CaseLawDTO save(CaseLawDTO dto) {
        var entity = caseLawMapper.toEntity(dto);
        var saved = caseLawRepository.save(entity);
        log.debug("Saved case law: {} - {}", saved.getCourt(), saved.getFilingNumber());
        return caseLawMapper.toDto(saved);
    }

    public void saveAll(List<CaseLawDTO> dtos) {
        var entities = caseLawMapper.toEntityList(dtos);
        caseLawRepository.saveAll(entities);
        log.debug("Batch saved {} case law records", dtos.size());
    }

    public void deleteById(Long id) {
        caseLawRepository.deleteById(id);
        log.debug("Deleted case law: {}", id);
    }
}
