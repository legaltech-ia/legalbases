package com.tys.legalbases.domain.model.entity;

import com.tys.legalbases.util.AppConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = AppConstants.TABLE_ARTICLES)
@Getter
@Setter
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_seq")
    @SequenceGenerator(name = "article_seq", sequenceName = "legal_bases.articulo_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = AppConstants.COLUMN_NORM_ID,
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_national_norm")
    )
    private NationalNormEntity nationalNorm;

    @Column(name = AppConstants.COLUMN_ARTICLE_NUMBER, nullable = false)
    private Integer articleNumber;

    @Column(name = AppConstants.COLUMN_ARTICLE_SUFFIX, length = AppConstants.ARTICLE_SUFFIX_LENGTH)
    private String articleSuffix;

    @Column(name = AppConstants.COLUMN_TITLE, length = AppConstants.ARTICLE_TITLE_LENGTH)
    private String title;

    @Column(name = AppConstants.COLUMN_CONTENT, nullable = false, columnDefinition = AppConstants.TEXT_COLUMN_DEFINITION)
    private String content;

    @Column(name = AppConstants.COLUMN_CREATED_AT, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
