package com.tys.legalbases.infrastructure.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Llena {@code article_label} en las filas escritas antes de que el campo
 * existiera.
 *
 * <p>Hace falta porque este servicio no tiene Flyway: el esquema lo mueve
 * {@code ddl-auto: update}, que añade la columna pero no toca los datos. Sin
 * esto, 243 artículos se quedan con la fuente de verdad en nulo y leyéndose
 * por la proyección legada, que es media migración —y la mitad que falta
 * aparece meses después, cuando alguien confía en el campo.
 *
 * <p>Se escribe la misma designación que derivaría {@code ArticleLabel}: el
 * entero más el sufijo. Es idempotente y solo toca lo que está en nulo, así
 * que arrancar dos veces no cuesta nada y una fila ya corregida a mano no se
 * pisa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "legalbases.backfill.article-label", havingValue = "true", matchIfMissing = true)
public class ArticleLabelBackfill {

    private static final String RELLENO = """
            UPDATE articles
               SET article_label = article_number::text || COALESCE(NULLIF(TRIM(article_suffix), ''), '')
             WHERE article_label IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void rellenarDesignaciones() {
        int filas = jdbcTemplate.update(RELLENO);
        log.info("Designacion de articulo: {} fila(s) sin article_label rellenadas desde la proyeccion legada", filas);
    }
}
