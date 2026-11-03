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
}
