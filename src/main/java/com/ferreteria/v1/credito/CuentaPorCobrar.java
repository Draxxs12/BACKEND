package com.ferreteria.v1.credito;

import java.math.BigDecimal;

public class CuentaPorCobrar {

    private BigDecimal saldo;
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE;

    public CuentaPorCobrar(BigDecimal saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void aplicarPago(BigDecimal monto) {
        saldo = saldo.subtract(monto);
    }

    public BigDecimal getSaldo()    { return saldo; }
    public EstadoCuenta getEstado() { return estado; }
}
