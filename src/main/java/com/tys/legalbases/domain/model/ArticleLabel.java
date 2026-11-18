package com.tys.legalbases.domain.model;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La designación de un artículo tal como la publica la norma.
 *
 * <p>Se guardaba como {@code Integer} más un sufijo, y eso solo da para una
 * numeración corrida: el 27 y el 223A de la Ley 1801. Un decreto reglamentario
 * numera {@code 2.2.8.18.7.1} —el artículo que fija la estructura obligatoria
 * del fallo es precisamente ese— y no cabe en un entero. Tampoco caben los
 * transitorios, ni la numeración romana de algunos reglamentos.
 *
 * <p>No es un caso raro del Decreto 768: en cuanto se alimentan más cerebros y
 * más cuerpos normativos —decretos, acuerdos municipales, circulares— la
 * numeración compuesta deja de ser la excepción. La designación es texto.
 *
 * <p>Esta clase traduce en los dos sentidos para no romper lo que ya hay: de
 * la designación a la proyección legada ({@code articleNumber} +
 * {@code articleSuffix}), y de vuelta para las filas que se escribieron antes
 * de que existiera la etiqueta.
 */
public final class ArticleLabel {

    /** El entero de cabeza y lo que le siga: "223A" -> 223 + "A"; "2.2.8.18.7.1" -> 2 + ".2.8.18.7.1". */
    private static final Pattern CABEZA_Y_RESTO = Pattern.compile("^\\s*(\\d+)(.*)$", Pattern.DOTALL);

    /** Lo que se guarda cuando la designación no empieza por un número ("Transitorio 1"). */
    public static final int SIN_NUMERO = 0;

    private ArticleLabel() {}

    /** La designación a partir de la proyección legada, para lo ya escrito. */
    public static String fromLegacy(Integer articleNumber, String articleSuffix) {
        String numero = Optional.ofNullable(articleNumber)
                .filter(value -> value != SIN_NUMERO)
                .map(String::valueOf)
                .orElse("");
        return numero + Optional.ofNullable(articleSuffix).map(String::trim).orElse("");
    }

    /**
     * El entero de ordenación. No identifica al artículo —el 2.2.8.18.7.1 y el
     * 2.2.8.18.2.1 comparten cabeza— solo conserva el orden y el índice que ya
     * existen sobre la columna.
     */
    public static Integer legacyNumber(String label) {
        return cabeza(label).map(Integer::parseInt).orElse(SIN_NUMERO);
    }

    /** El resto de la designación, que es lo que de verdad la distingue. */
    public static String legacySuffix(String label) {
        return Optional.ofNullable(label)
                .map(CABEZA_Y_RESTO::matcher)
                .filter(Matcher::find)
                .map(m -> m.group(2))
                .orElseGet(() -> Optional.ofNullable(label).orElse(""));
    }

    private static Optional<String> cabeza(String label) {
        return Optional.ofNullable(label)
                .map(CABEZA_Y_RESTO::matcher)
                .filter(Matcher::find)
                .map(m -> m.group(1));
    }
}
