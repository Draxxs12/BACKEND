package com.ferreteria.v1;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** HU-S2-17: reglas de validación de producto (pruebas puras, sin Spring ni base de datos). */
class ValidadorTest {

    @Test
    void noNegativo_aceptaCero_valorLimite() {
        assertDoesNotThrow(() -> Validador.noNegativo(0, "stock"));
    }

    @Test
    void noNegativo_aceptaNulo() {
        assertDoesNotThrow(() -> Validador.noNegativo(null, "stock"));
    }

    @Test
    void noNegativo_rechazaNegativo() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> Validador.noNegativo(-1, "stock"));
        assertTrue(ex.getMessage().contains("stock"));
    }

    @Test
    void noNegativo_rechazaDecimalNegativo() {
        assertThrows(ValidacionException.class, () -> Validador.noNegativo(-0.01, "precio de venta"));
    }

    @Test
    void ventaMayorIgualCompra_aceptaIguales_valorLimite() {
        assertDoesNotThrow(() -> Validador.ventaMayorIgualCompra(100.0, 100.0));
    }

    @Test
    void ventaMayorIgualCompra_rechazaVentaMenorQueCompra() {
        assertThrows(ValidacionException.class, () -> Validador.ventaMayorIgualCompra(50.0, 100.0));
    }

    @Test
    void ventaMayorIgualCompra_rechazaDiferenciaMinima() {
        assertThrows(ValidacionException.class, () -> Validador.ventaMayorIgualCompra(99.99, 100.0));
    }

    @Test
    void documento_dniDe8DigitosEsValido() {
        assertDoesNotThrow(() -> Validador.documento("DNI", "70999888"));
    }

    @Test
    void documento_dniDe7Y9DigitosSeRechaza() {
        assertThrows(ValidacionException.class, () -> Validador.documento("DNI", "7099988"));
        assertThrows(ValidacionException.class, () -> Validador.documento("DNI", "709998888"));
    }

    /**
     * CANDIDATO A INCIDENCIA (confirma la regla de negocio con tu docente/cliente):
     * el RUC peruano tiene 11 dígitos, pero Validador.documento exige exactamente 20.
     * Hoy esta prueba FALLA (ROJO).
     */
    @Test
    void documento_rucDe11DigitosDeberiaSerValido() {
        assertDoesNotThrow(() -> Validador.documento("RUC", "20123456789"));
    }
}
