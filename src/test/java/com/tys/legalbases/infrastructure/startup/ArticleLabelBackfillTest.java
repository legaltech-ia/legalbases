package com.tys.legalbases.infrastructure.startup;

import com.tys.legalbases.domain.model.ArticleLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Este servicio no tiene Flyway: {@code ddl-auto: update} añade la columna y
 * no toca los datos, así que las 243 filas ya escritas se quedaban con la
 * fuente de verdad en nulo. El relleno es la migración de datos que falta.
 */
@ExtendWith(MockitoExtension.class)
class ArticleLabelBackfillTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private String sentencia() {
        when(jdbcTemplate.update(anyString())).thenReturn(243);
        new ArticleLabelBackfill(jdbcTemplate).rellenarDesignaciones();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sql.capture());
        return sql.getValue();
    }

    /** Solo lo que está en nulo: una fila ya corregida a mano no se pisa. */
    @Test
    void onlyRowsWithoutADesignationAreTouched() {
        assertThat(sentencia()).containsIgnoringCase("where article_label is null");
    }

    /** Y lo que escribe tiene que ser lo mismo que derivaria el codigo. */
    @Test
    void theBackfillWritesTheSameDesignationTheCodeDerives() {
        String sql = sentencia();

        assertThat(sql).contains("article_number").contains("article_suffix");
        // El contrato que replica la sentencia, fijado aqui para que no se separen.
        assertThat(ArticleLabel.fromLegacy(223, "A")).isEqualTo("223A");
        assertThat(ArticleLabel.fromLegacy(27, null)).isEqualTo("27");
        assertThat(ArticleLabel.fromLegacy(27, "   ")).isEqualTo("27");
    }
}
