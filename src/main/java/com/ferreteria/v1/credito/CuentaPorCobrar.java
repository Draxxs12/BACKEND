package com.ferreteria.v1.credito;

import java.math.BigDecimal;

import com.ferreteria.v1.exceptions.ValidacionException;

public class CuentaPorCobrar {

    private BigDecimal saldo;
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE;

    public CuentaPorCobrar(BigDecimal saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void aplicarPago(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new ValidacionException("El monto del pago debe ser mayor que 0.");
        }
        if (monto.compareTo(saldo) > 0) {
            throw new ValidacionException("El pago no puede ser mayor que el saldo pendiente.");
        }
        saldo = saldo.subtract(monto);
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            estado = EstadoCuenta.PAGADA;
        }
    }

    public BigDecimal getSaldo()    { return saldo; }
    public EstadoCuenta getEstado() { return estado; }
}
