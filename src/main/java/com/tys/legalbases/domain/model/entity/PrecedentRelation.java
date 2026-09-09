package com.tys.legalbases.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "precedent_relations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrecedentRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurisprudencia_origen_id", nullable = false)
    private CaseLawEntity sourceCaseLaw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurisprudencia_destino_id", nullable = false)
    private CaseLawEntity targetCaseLaw;

    @Column(name = "tipo_relacion", nullable = false)
    private String relationType;

    @Column(name = "contexto")
    private String context;
}
