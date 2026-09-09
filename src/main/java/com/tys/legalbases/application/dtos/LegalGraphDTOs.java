package com.tys.legalbases.application.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for the sources-of-law graph.
 */
public final class LegalGraphDTOs {

    private LegalGraphDTOs() {}

    public record NodeRequest(
            String type,
            String reference,
            String status,
            String cardId,
            String sourceS3,
            Long nationalNormId
    ) {}

    public record NodeResponse(
            Long id,
            String type,
            String reference,
            String status,
            String cardId,
            String sourceS3,
            Long nationalNormId
    ) {}

    public record EdgeRequest(
            String sourceReference,
            String targetReference,
            String relationType,
            LocalDate effectiveFrom
    ) {}

    public record RelationView(
            String relationType,
            Long nodeId,
            String reference,
            String status
    ) {}

    public record Effect(
            String source,
            String relationType,
            String target,
            Integer level
    ) {}

    /**
     * The "enriched norm": the node + its status + who affects it and whom
     * it affects. This is the view agents consume before citing.
     */
    public record EnrichedNorm(
            NodeResponse node,
            List<RelationView> affectedBy,
            List<RelationView> affects,
            List<Effect> effectChain
    ) {}
}
