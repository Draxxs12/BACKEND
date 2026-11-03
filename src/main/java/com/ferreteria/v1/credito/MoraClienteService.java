package com.ferreteria.v1.credito;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MoraClienteService {

    static final int DIAS_AVISO = 3;   // unica fuente del plazo de aviso

    public EstadoMora calcularEstado(BigDecimal saldoPendiente, LocalDate vencimiento, LocalDate hoy) {
        if (!tieneDeuda(saldoPendiente))     return EstadoMora.AL_DIA;
        if (estaVencida(vencimiento, hoy))   return EstadoMora.MOROSO;
        if (estaPorVencer(vencimiento, hoy)) return EstadoMora.POR_VENCER;
        return EstadoMora.AL_DIA;
    }

    private boolean tieneDeuda(BigDecimal saldo) {
        return saldo != null && saldo.signum() > 0;
    }

    private boolean estaVencida(LocalDate vencimiento, LocalDate hoy) {
        return hoy.isAfter(vencimiento);
    }

    private boolean estaPorVencer(LocalDate vencimiento, LocalDate hoy) {
        return !hoy.plusDays(DIAS_AVISO).isBefore(vencimiento);
    }
}
