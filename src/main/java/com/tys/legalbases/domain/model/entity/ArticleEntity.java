package com.tys.legalbases.domain.model.entity;

import com.tys.legalbases.util.AppConstants;
import com.tys.legalbases.domain.model.ArticleLabel;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
import java.util.Optional;

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

    /**
     * La designación del artículo tal como la publica la norma: "27", "223A",
     * "2.2.8.18.7.1". Es la fuente de verdad desde que entraron decretos con
     * numeración compuesta, que no cabe en un entero.
     */
    @Column(name = AppConstants.COLUMN_ARTICLE_LABEL, length = AppConstants.ARTICLE_LABEL_LENGTH)
    private String articleLabel;

    /**
     * @deprecated proyección de {@link #articleLabel}, conservada porque el
     *     índice {@code idx_articulo_lookup} y los consumidores existentes la
     *     leen. No identifica al artículo: el 2.2.8.18.7.1 y el 2.2.8.18.2.1
     *     comparten este entero. Ordena, nada más.
     */
    @Deprecated
    @Column(name = AppConstants.COLUMN_ARTICLE_NUMBER, nullable = false)
    private Integer articleNumber;

    /** @deprecated proyección de {@link #articleLabel}; ver {@link #articleNumber}. */
    @Deprecated
    @Column(name = AppConstants.COLUMN_ARTICLE_SUFFIX, length = AppConstants.ARTICLE_SUFFIX_LENGTH)
    private String articleSuffix;

    /**
     * Las dos representaciones se mantienen a la par aquí y no en cada
     * llamador: una fila escrita por la vía vieja gana su designación, y una
     * escrita con designación gana su proyección. Así ningún camino de
     * ingesta queda a medias.
     */
    @PrePersist
    @PreUpdate
    void sincronizarDesignacion() {
        Optional.ofNullable(articleLabel)
                .filter(label -> !label.isBlank())
                .ifPresentOrElse(this::proyectarDesdeDesignacion, this::deducirDesignacion);
    }

    @SuppressWarnings("deprecation")
    private void proyectarDesdeDesignacion(String label) {
        this.articleNumber = ArticleLabel.legacyNumber(label);
        this.articleSuffix = ArticleLabel.legacySuffix(label);
    }

    @SuppressWarnings("deprecation")
    private void deducirDesignacion() {
        this.articleLabel = ArticleLabel.fromLegacy(articleNumber, articleSuffix);
    }

    @Column(name = AppConstants.COLUMN_TITLE, length = AppConstants.ARTICLE_TITLE_LENGTH)
    private String title;

    @Column(name = AppConstants.COLUMN_CONTENT, nullable = false, columnDefinition = AppConstants.TEXT_COLUMN_DEFINITION)
    private String content;

    @Column(name = AppConstants.COLUMN_CREATED_AT, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
