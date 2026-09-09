package com.tys.legalbases.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Graph edge: "source [relationType] target".
 * E.g.: sentencia C-282/2017 [condiciona] Ley 1801 art. 27.
 */
@Entity
@Table(name = "legal_edges", uniqueConstraints = {
        @UniqueConstraint(name = "uk_legal_edge", columnNames = {"source_id", "target_id", "relation_type"})
})
@Getter
@Setter
public class LegalEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private LegalNodeEntity source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private LegalNodeEntity target;

    /** deroga | modifica | interpreta | condiciona | cita */
    @Column(name = "relation_type", nullable = false, length = 30)
    private String relationType;

    /** Date the relation takes effect from. */
    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
}
