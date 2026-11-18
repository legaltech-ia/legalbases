package com.tys.legalbases.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La designación de un artículo es texto, no un entero. Cabía el 27 y cabía el
 * 223A con sufijo, pero no el 2.2.8.18.7.1 del Decreto 768 —justo el que fija
 * la estructura obligatoria del fallo—, y en cuanto entren más decretos,
 * acuerdos y circulares la numeración compuesta deja de ser la excepción.
 */
class ArticleLabelTest {

    @ParameterizedTest(name = "{0} -> numero {1}, sufijo \"{2}\"")
    @CsvSource(delimiter = '|', value = {
            "27|27|",
            "223A|223|A",
            "2.2.8.18.7.1|2|.2.8.18.7.1",
            "2.2.8.18.2.1|2|.2.8.18.2.1"})
    void aLabelProjectsOntoTheLegacyColumns(String label, int numero, String sufijo) {
        assertThat(ArticleLabel.legacyNumber(label)).isEqualTo(numero);
        assertThat(ArticleLabel.legacySuffix(label)).isEqualTo(sufijo == null ? "" : sufijo);
    }

    /** Y la vuelta tiene que devolver lo mismo, o las filas viejas se leen mal. */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"27|27", "223A|223A", "2.2.8.18.7.1|2.2.8.18.7.1"})
    void theRoundTripKeepsTheLabel(String label, String esperado) {
        assertThat(ArticleLabel.fromLegacy(
                ArticleLabel.legacyNumber(label), ArticleLabel.legacySuffix(label))).isEqualTo(esperado);
    }

    /** Lo ya escrito: numero mas sufijo opcional. */
    @Test
    void anOldRowStillReadsAsItsLabel() {
        assertThat(ArticleLabel.fromLegacy(223, "A")).isEqualTo("223A");
        assertThat(ArticleLabel.fromLegacy(27, null)).isEqualTo("27");
        assertThat(ArticleLabel.fromLegacy(27, "  ")).isEqualTo("27");
    }

    /**
     * Una designación que no empieza por número existe --los transitorios-- y
     * no puede tumbar la escritura: el entero de ordenación queda en cero y la
     * designación entera viaja en el sufijo.
     */
    @Test
    void aLabelWithoutALeadingNumberDoesNotBreak() {
        assertThat(ArticleLabel.legacyNumber("Transitorio 1")).isEqualTo(ArticleLabel.SIN_NUMERO);
        assertThat(ArticleLabel.legacySuffix("Transitorio 1")).isEqualTo("Transitorio 1");
        assertThat(ArticleLabel.fromLegacy(ArticleLabel.SIN_NUMERO, "Transitorio 1")).isEqualTo("Transitorio 1");
    }

    /** El 2.2.8.18.7.1 y el 2.2.8.18.2.1 comparten cabeza: el entero no identifica. */
    @Test
    void theLeadingNumberOrdersButDoesNotIdentify() {
        assertThat(ArticleLabel.legacyNumber("2.2.8.18.7.1"))
                .isEqualTo(ArticleLabel.legacyNumber("2.2.8.18.2.1"));
        assertThat(ArticleLabel.legacySuffix("2.2.8.18.7.1"))
                .isNotEqualTo(ArticleLabel.legacySuffix("2.2.8.18.2.1"));
    }
}
