package com.tys.legalbases.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Node of the sources-of-law graph: a law, an article, a decree, a ruling or
 * a concept. Status lives here, and it's what agents must check before
 * citing any norm - restored from git history (deleted in b85146a without a
 * deprecation reason; see docs/superpowers/plans/2026-07-23-... audit).
 */
@Entity
@Table(name = "legal_nodes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_legal_node_reference", columnNames = "reference")
})
@Getter
@Setter
public class LegalNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ley | articulo | decreto | sentencia | concepto */
    @Column(nullable = false, length = 30)
    private String type;

    /** Ej: "Ley 1801 de 2016, art. 27" | "C-282 de 2017" */
    @Column(nullable = false, length = 300)
    private String reference;

    /** vigente | modificado | derogado | condicionado */
    @Column(nullable = false, length = 30)
    private String status = "vigente";

    /** Pointer to the distilled card in the vector corpus (legal service). */
    @Column(name = "card_id", length = 100)
    private String cardId;

    /** Original source document in S3. */
    @Column(name = "source_s3", columnDefinition = "TEXT")
    private String sourceS3;

    /** Optional link to the norm registered in national_norms. */
    @Column(name = "national_norm_id")
    private Long nationalNormId;
}
