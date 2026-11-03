package com.ferreteria.v1.credito;

import java.math.BigDecimal;

/** Esqueleto (fase Red): aplicarPago todavia no hace nada. */
public class CuentaPorCobrar {

    private BigDecimal saldo;
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE;

    public CuentaPorCobrar(BigDecimal saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void aplicarPago(BigDecimal monto) {
    }

    public BigDecimal getSaldo()    { return saldo; }
    public EstadoCuenta getEstado() { return estado; }
}
