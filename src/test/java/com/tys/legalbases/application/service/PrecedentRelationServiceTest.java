package com.tys.legalbases.application.service;

import com.tys.legalbases.domain.exception.ResourceNotFoundException;
import com.tys.legalbases.domain.model.entity.CaseLawEntity;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrecedentRelationServiceTest {

    @Mock
    private CaseLawRepository caseLawRepository;

    @InjectMocks
    private PrecedentRelationService precedentRelationService;

    @Test
    void addPrecedentRelationThrowsResourceNotFoundWhenSourceMissing() {
        when(caseLawRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> precedentRelationService.addPrecedentRelation(1L, 2L, "REITERA", "context"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Source case law not found: 1");
        verify(caseLawRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addPrecedentRelationSavesRelationWiringSourceAndTarget() {
        var source = CaseLawEntity.builder().id(1L).filingNumber("T-100/24").build();
        var target = CaseLawEntity.builder().id(2L).filingNumber("T-200/24").build();
        when(caseLawRepository.findById(1L)).thenReturn(Optional.of(source));
        when(caseLawRepository.findById(2L)).thenReturn(Optional.of(target));
        when(caseLawRepository.save(source)).thenReturn(source);

        precedentRelationService.addPrecedentRelation(1L, 2L, "REITERA", "shared ratio decidendi");

        var captor = ArgumentCaptor.forClass(CaseLawEntity.class);
        verify(caseLawRepository).save(captor.capture());
        assertThat(captor.getValue().getOutgoingPrecedents())
                .singleElement()
                .satisfies(relation -> {
                    assertThat(relation.getSourceCaseLaw()).isSameAs(source);
                    assertThat(relation.getTargetCaseLaw()).isSameAs(target);
                    assertThat(relation.getRelationType()).isEqualTo("REITERA");
                    assertThat(relation.getContext()).isEqualTo("shared ratio decidendi");
                });
    }
}
