package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.LegalGraphDTOs.EdgeRequest;
import com.tys.legalbases.application.dtos.LegalGraphDTOs.NodeRequest;
import com.tys.legalbases.domain.model.entity.LegalNodeEntity;
import com.tys.legalbases.domain.model.repository.LegalEdgeRepository;
import com.tys.legalbases.domain.model.repository.LegalNodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegalGraphServiceTest {

    @Mock
    private LegalNodeRepository nodeRepository;

    @Mock
    private LegalEdgeRepository edgeRepository;

    @InjectMocks
    private LegalGraphService graphService;

    private LegalNodeEntity node(Long id, String reference, String status) {
        LegalNodeEntity n = new LegalNodeEntity();
        n.setId(id);
        n.setType("ley");
        n.setReference(reference);
        n.setStatus(status);
        return n;
    }

    @Test
    void createNodeRejectsAnUnknownType() {
        assertThatThrownBy(() -> graphService.createNode(
                new NodeRequest("reglamento", "X", null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createNodeIsIdempotentByReference() {
        LegalNodeEntity existing = node(1L, "Ley 1801 de 2016", "vigente");
        when(nodeRepository.findByReferenceIgnoreCase("Ley 1801 de 2016")).thenReturn(Optional.of(existing));
        when(nodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        graphService.createNode(new NodeRequest("ley", "Ley 1801 de 2016", null, null, null, null));

        ArgumentCaptor<LegalNodeEntity> captor = ArgumentCaptor.forClass(LegalNodeEntity.class);
        org.mockito.Mockito.verify(nodeRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void createEdgeRejectsAnUnknownRelationType() {
        assertThatThrownBy(() -> graphService.createEdge(
                new EdgeRequest("A", "B", "cambia", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createEdgeFailsWhenSourceReferenceIsNotRegistered() {
        when(nodeRepository.findByReferenceIgnoreCase("A")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> graphService.createEdge(new EdgeRequest("A", "B", "deroga", null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void derogaMarksTheTargetAsDerogado() {
        LegalNodeEntity source = node(1L, "Decreto 999 de 2026", "vigente");
        LegalNodeEntity target = node(2L, "Ley 1801 de 2016, art. 27", "vigente");
        when(nodeRepository.findByReferenceIgnoreCase("Decreto 999 de 2026")).thenReturn(Optional.of(source));
        when(nodeRepository.findByReferenceIgnoreCase("Ley 1801 de 2016, art. 27")).thenReturn(Optional.of(target));
        stubEmptyEnrichment(target);

        graphService.createEdge(new EdgeRequest("Decreto 999 de 2026", "Ley 1801 de 2016, art. 27", "deroga", null));

        assertThat(target.getStatus()).isEqualTo("derogado");
    }

    @Test
    void modificaOnlyDowngradesFromVigente() {
        LegalNodeEntity source = node(1L, "Ley 2197 de 2022", "vigente");
        LegalNodeEntity yaDerogado = node(2L, "Norma ya derogada", "derogado");
        when(nodeRepository.findByReferenceIgnoreCase("Ley 2197 de 2022")).thenReturn(Optional.of(source));
        when(nodeRepository.findByReferenceIgnoreCase("Norma ya derogada")).thenReturn(Optional.of(yaDerogado));
        stubEmptyEnrichment(yaDerogado);

        // "modifica" no debe revivir una norma ya derogada como "modificado".
        graphService.createEdge(new EdgeRequest("Ley 2197 de 2022", "Norma ya derogada", "modifica", null));

        assertThat(yaDerogado.getStatus()).isEqualTo("derogado");
    }

    @Test
    void citaNeverChangesStatus() {
        LegalNodeEntity source = node(1L, "Sentencia C-282/2017", "vigente");
        LegalNodeEntity target = node(2L, "Ley 1801 de 2016, art. 27", "vigente");
        when(nodeRepository.findByReferenceIgnoreCase("Sentencia C-282/2017")).thenReturn(Optional.of(source));
        when(nodeRepository.findByReferenceIgnoreCase("Ley 1801 de 2016, art. 27")).thenReturn(Optional.of(target));
        stubEmptyEnrichment(target);

        graphService.createEdge(new EdgeRequest("Sentencia C-282/2017", "Ley 1801 de 2016, art. 27", "cita", null));

        assertThat(target.getStatus()).isEqualTo("vigente");
    }

    /** createEdge ends by re-reading the enriched norm; stub that leg with an empty graph. */
    private void stubEmptyEnrichment(LegalNodeEntity target) {
        lenient().when(nodeRepository.findById(target.getId())).thenReturn(Optional.of(target));
        lenient().when(edgeRepository.findByTargetId(target.getId())).thenReturn(List.of());
        lenient().when(edgeRepository.findBySourceId(target.getId())).thenReturn(List.of());
        lenient().when(nodeRepository.effectChain(target.getId())).thenReturn(List.of());
    }

    @Test
    void findByNationalNormIdIsTheJoinKeyLegalUsesForItsVigenciaCheck() {
        LegalNodeEntity n = node(2L, "Ley 1801 de 2016, art. 27", "derogado");
        n.setNationalNormId(42L);
        when(nodeRepository.findByNationalNormId(42L)).thenReturn(List.of(n));

        var result = graphService.findByNationalNormId(42L);

        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.nationalNormId()).isEqualTo(42L);
            assertThat(r.status()).isEqualTo("derogado");
        });
    }

    @Test
    void enrichedNormAssemblesWhatAffectsAndWhatIsAffected() {
        LegalNodeEntity n = node(2L, "Ley 1801 de 2016, art. 27", "modificado");
        when(nodeRepository.findById(2L)).thenReturn(Optional.of(n));
        when(edgeRepository.findByTargetId(2L)).thenReturn(List.of());
        when(edgeRepository.findBySourceId(2L)).thenReturn(List.of());
        when(nodeRepository.effectChain(2L)).thenReturn(List.of());

        var enriched = graphService.enrichedNorm(2L);

        assertThat(enriched.node().reference()).isEqualTo("Ley 1801 de 2016, art. 27");
        assertThat(enriched.node().status()).isEqualTo("modificado");
        assertThat(enriched.affectedBy()).isEmpty();
        assertThat(enriched.affects()).isEmpty();
    }
}
