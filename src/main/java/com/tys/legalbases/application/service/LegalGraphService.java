package com.tys.legalbases.application.service;

import com.tys.legalbases.application.dtos.LegalGraphDTOs.*;
import com.tys.legalbases.domain.model.entity.LegalEdgeEntity;
import com.tys.legalbases.domain.model.entity.LegalNodeEntity;
import com.tys.legalbases.domain.model.repository.LegalEdgeRepository;
import com.tys.legalbases.domain.model.repository.LegalNodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Sources-of-law graph. Central rule: creating a deroga/modifica/condiciona
 * edge automatically updates the target node's status - status is never
 * hand-edited, the graph produces it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LegalGraphService {

    private static final Set<String> NODE_TYPES =
            Set.of("ley", "articulo", "decreto", "sentencia", "concepto");
    private static final Set<String> RELATION_TYPES =
            Set.of("deroga", "modifica", "interpreta", "condiciona", "cita");

    private final LegalNodeRepository nodeRepository;
    private final LegalEdgeRepository edgeRepository;

    @Transactional
    public NodeResponse createNode(NodeRequest request) {
        if (request.type() == null || !NODE_TYPES.contains(request.type())) {
            throw new IllegalArgumentException("type debe ser uno de " + NODE_TYPES);
        }
        if (request.reference() == null || request.reference().isBlank()) {
            throw new IllegalArgumentException("reference es obligatoria");
        }
        // Idempotent by reference: the curator can retry without duplicating.
        LegalNodeEntity node = nodeRepository
                .findByReferenceIgnoreCase(request.reference().trim())
                .orElseGet(LegalNodeEntity::new);

        node.setType(request.type());
        node.setReference(request.reference().trim());
        if (request.status() != null) {
            node.setStatus(request.status());
        }
        node.setCardId(request.cardId());
        node.setSourceS3(request.sourceS3());
        node.setNationalNormId(request.nationalNormId());
        return toResponse(nodeRepository.save(node));
    }

    @Transactional
    public EnrichedNorm createEdge(EdgeRequest request) {
        if (request.relationType() == null || !RELATION_TYPES.contains(request.relationType())) {
            throw new IllegalArgumentException("relationType debe ser uno de " + RELATION_TYPES);
        }
        LegalNodeEntity source = findByReference(request.sourceReference());
        LegalNodeEntity target = findByReference(request.targetReference());

        if (!edgeRepository.existsBySourceIdAndTargetIdAndRelationType(
                source.getId(), target.getId(), request.relationType())) {
            LegalEdgeEntity edge = new LegalEdgeEntity();
            edge.setSource(source);
            edge.setTarget(target);
            edge.setRelationType(request.relationType());
            edge.setEffectiveFrom(request.effectiveFrom());
            edgeRepository.save(edge);
        }

        // The target's status is produced by the graph:
        switch (request.relationType()) {
            case "deroga" -> target.setStatus("derogado");
            case "modifica" -> {
                if ("vigente".equals(target.getStatus())) {
                    target.setStatus("modificado");
                }
            }
            case "condiciona" -> {
                if (!"derogado".equals(target.getStatus())) {
                    target.setStatus("condicionado");
                }
            }
            default -> { /* interpreta y cita no alteran vigencia */ }
        }
        nodeRepository.save(target);
        log.info("Edge: {} [{}] {} -> target status: {}",
                source.getReference(), request.relationType(), target.getReference(), target.getStatus());

        return enrichedNorm(target.getId());
    }

    @Transactional(readOnly = true)
    public EnrichedNorm enrichedNorm(Long nodeId) {
        LegalNodeEntity node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new EntityNotFoundException("Nodo " + nodeId + " no existe"));

        List<RelationView> affectedBy = edgeRepository.findByTargetId(nodeId).stream()
                .map(e -> new RelationView(e.getRelationType(), e.getSource().getId(),
                        e.getSource().getReference(), e.getSource().getStatus()))
                .toList();
        List<RelationView> affects = edgeRepository.findBySourceId(nodeId).stream()
                .map(e -> new RelationView(e.getRelationType(), e.getTarget().getId(),
                        e.getTarget().getReference(), e.getTarget().getStatus()))
                .toList();
        List<Effect> chain = nodeRepository.effectChain(nodeId).stream()
                .map(p -> new Effect(p.getSource(), p.getRelationType(), p.getTarget(), p.getLevel()))
                .toList();

        return new EnrichedNorm(toResponse(node), affectedBy, affects, chain);
    }

    @Transactional(readOnly = true)
    public List<NodeResponse> searchNodes(String reference) {
        return nodeRepository.findByReferenceContainingIgnoreCase(reference).stream()
                .map(LegalGraphService::toResponse)
                .toList();
    }

    private LegalNodeEntity findByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Las referencias de origen y destino son obligatorias");
        }
        return nodeRepository.findByReferenceIgnoreCase(reference.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe un nodo con referencia '" + reference + "'. Créalo primero."));
    }

    private static NodeResponse toResponse(LegalNodeEntity node) {
        return new NodeResponse(node.getId(), node.getType(), node.getReference(),
                node.getStatus(), node.getCardId(), node.getSourceS3(), node.getNationalNormId());
    }
}
