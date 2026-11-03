package com.ferreteria.v1.credito;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.ferreteria.v1.exceptions.ValidacionException;

/** HU-S3-01 (RF-16): una venta al crédito exige una fecha de vencimiento válida. */
class PoliticaCreditoTest {

    private final PoliticaCredito politica = new PoliticaCredito();
    private final LocalDate hoy = LocalDate.of(2026, 10, 27);

    @Test
    void ventaAlCreditoSinFechaDeVencimientoSeRechaza() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> politica.validarVencimiento(null, hoy));

        assertEquals("Indica la fecha de vencimiento del crédito.", ex.getMessage());
    }
}
