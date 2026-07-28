package com.tys.legalbases.domain.model.entity;

import com.tys.legalbases.util.AppConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = AppConstants.TABLE_NATIONAL_NORMS)
@Getter
@Setter
public class NationalNormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = AppConstants.COLUMN_TITLE, nullable = false, length = AppConstants.TITLE_LENGTH)
    private String title;

    @Column(name = AppConstants.COLUMN_DESCRIPTION, columnDefinition = AppConstants.TEXT_COLUMN_DEFINITION)
    private String description;

    @Column(name = AppConstants.COLUMN_TYPE, nullable = false, length = AppConstants.TYPE_LENGTH)
    private String type;

    @Column(name = AppConstants.COLUMN_PUBLISHED_AT, columnDefinition = AppConstants.TIMESTAMP_WITH_TIME_ZONE_DEFINITION)
    private LocalDateTime publishedAt;

    @Column(name = AppConstants.COLUMN_SOURCE_URL, nullable = false, columnDefinition = AppConstants.TEXT_COLUMN_DEFINITION)
    private String sourceUrl;

    @OneToMany(mappedBy = "nationalNorm", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleEntity> articles;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "numero", length = 100)
    private String numero;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "subtipo", length = 100)
    private String subtipo;

    @Column(name = "vigencia", length = 50)
    private String vigencia;

    @Column(name = "entidad", length = 200)
    private String entidad;

    @Column(name = "materia", columnDefinition = "TEXT")
    private String materia;

    @Column(name = "articulos", length = 50)
    private String articulos;
}
