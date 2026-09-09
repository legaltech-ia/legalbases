package com.tys.legalbases.application.service;

import com.tys.legalbases.domain.exception.ResourceNotFoundException;
import com.tys.legalbases.domain.model.entity.PrecedentRelation;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrecedentRelationService {

    private final CaseLawRepository caseLawRepository;

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
}
