package com.tys.legalbases.domain.model.repository;

import com.tys.legalbases.domain.model.entity.LegalNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LegalNodeRepository extends JpaRepository<LegalNodeEntity, Long> {

    Optional<LegalNodeEntity> findByReferenceIgnoreCase(String reference);

    List<LegalNodeEntity> findByReferenceContainingIgnoreCase(String reference);

    /**
     * Transitive chain of effects reaching a node: which norms or rulings
     * derogate, modify or condition it, directly or through others. Walks
     * the graph backwards with a recursive CTE (max 5 levels).
     */
    @Query(value = """
            WITH RECURSIVE effects AS (
                SELECT e.source_id, e.target_id, e.relation_type, 1 AS level
                FROM legal_bases.legal_edges e
                WHERE e.target_id = :nodeId
                  AND e.relation_type IN ('deroga', 'modifica', 'condiciona')
                UNION ALL
                SELECT e2.source_id, e2.target_id, e2.relation_type, ef.level + 1
                FROM legal_bases.legal_edges e2
                JOIN effects ef ON e2.target_id = ef.source_id
                WHERE ef.level < 5
                  AND e2.relation_type IN ('deroga', 'modifica', 'condiciona')
            )
            SELECT n.reference AS source, ef.relation_type AS relationType,
                   d.reference AS target, ef.level AS level
            FROM effects ef
            JOIN legal_bases.legal_nodes n ON n.id = ef.source_id
            JOIN legal_bases.legal_nodes d ON d.id = ef.target_id
            ORDER BY ef.level
            """, nativeQuery = true)
    List<EffectProjection> effectChain(@Param("nodeId") Long nodeId);

    interface EffectProjection {
        String getSource();
        String getRelationType();
        String getTarget();
        Integer getLevel();
    }
}
