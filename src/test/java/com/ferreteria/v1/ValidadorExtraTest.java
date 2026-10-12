package com.ferreteria.v1;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.exceptions.Validador;

/** CP-230 (documento con letras) y reglas de correo. */
class ValidadorExtraTest {

    @Test
    void documento_dniConLetrasSeRechaza() {
        assertThrows(ValidacionException.class, () -> Validador.documento("DNI", "1234567A"));
    }

    @Test
    void documento_rucConLetrasSeRechaza() {
        assertThrows(ValidacionException.class, () -> Validador.documento("RUC", "2012345678X"));
    }

    @Test
    void documento_nuloOEnBlancoSeIgnora() {
        assertDoesNotThrow(() -> Validador.documento("DNI", null));
        assertDoesNotThrow(() -> Validador.documento("DNI", "  "));
    }

    @Test
    void documento_rucDe10Y12DigitosSeRechazan_valoresLimite() {
        assertThrows(ValidacionException.class, () -> Validador.documento("RUC", "2012345678"));
        assertThrows(ValidacionException.class, () -> Validador.documento("RUC", "201234567890"));
    }

    @Test
    void email_formatoValidoSeAcepta() {
        assertDoesNotThrow(() -> Validador.email("ana@mail.com"));
    }

    @Test
    void email_nuloOEnBlancoSeAcepta() {
        assertDoesNotThrow(() -> Validador.email(null));
        assertDoesNotThrow(() -> Validador.email(""));
    }

    @Test
    void email_sinArrobaOSinDominioSeRechaza() {
        assertThrows(ValidacionException.class, () -> Validador.email("sin-arroba"));
        assertThrows(ValidacionException.class, () -> Validador.email("ana@mail"));
    }
}
