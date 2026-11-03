package com.ferreteria.v1.credito;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ferreteria.v1.exceptions.ValidacionException;

/** HU-S3-03 (RF-18): pago total o parcial de una deuda. */
class CuentaPorCobrarTest {

    private CuentaPorCobrar cuenta500() {
        return new CuentaPorCobrar(new BigDecimal("500.00"));
    }

    private void assertSaldo(String esperado, CuentaPorCobrar c) {
        assertEquals(0, new BigDecimal(esperado).compareTo(c.getSaldo()), "saldo actual: " + c.getSaldo());
    }

    @Test
    void pagoParcialDescuentaDelSaldoYLaCuentaSigueEnPendiente() {
        CuentaPorCobrar cuenta = cuenta500();

        cuenta.aplicarPago(new BigDecimal("200.00"));

        assertSaldo("300.00", cuenta);
        assertEquals(EstadoCuenta.PENDIENTE, cuenta.getEstado());
    }

    @Test
    void pagoDelSaldoExactoDejaLaCuentaPagada_valorLimite() {
        CuentaPorCobrar cuenta = cuenta500();

        cuenta.aplicarPago(new BigDecimal("500.00"));

        assertSaldo("0.00", cuenta);
        assertEquals(EstadoCuenta.PAGADA, cuenta.getEstado());
    }

    @Test
    void pagoMayorAlSaldoSeRechaza_valorLimite() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> cuenta500().aplicarPago(new BigDecimal("500.01")));

        assertEquals("El pago no puede ser mayor que el saldo pendiente.", ex.getMessage());
    }

    @Test
    void pagoDeCeroSeRechaza_valorLimite() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> cuenta500().aplicarPago(BigDecimal.ZERO));

        assertEquals("El monto del pago debe ser mayor que 0.", ex.getMessage());
    }

    @Test
    void pagoNegativoSeRechaza() {
        assertThrows(ValidacionException.class, () -> cuenta500().aplicarPago(new BigDecimal("-5")));
    }

    @Test
    void siElPagoSeRechazaElSaldoQuedaIntacto() {
        CuentaPorCobrar cuenta = cuenta500();

        assertThrows(ValidacionException.class, () -> cuenta.aplicarPago(new BigDecimal("999.00")));

        assertSaldo("500.00", cuenta);
        assertEquals(EstadoCuenta.PENDIENTE, cuenta.getEstado());
    }
}
