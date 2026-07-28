package com.tys.legalbases.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jurisprudencia")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseLawEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corte", nullable = false)
    private String court;

    @Column(name = "sala")
    private String chamber;

    @Column(name = "radicado", nullable = false)
    private String filingNumber;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime date;

    @Column(name = "ponente")
    private String rapporteur;

    @Column(name = "tema_principal", nullable = false)
    private String mainTopic;

    @Column(name = "tipo_decision", nullable = false)
    private String decisionType;

    @Column(name = "texto_completo", columnDefinition = "TEXT")
    private String fullText;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "url_fuente")
    private String sourceUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sourceCaseLaw", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrecedentRelation> outgoingPrecedents = new ArrayList<>();

    @OneToMany(mappedBy = "targetCaseLaw", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrecedentRelation> incomingPrecedents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
