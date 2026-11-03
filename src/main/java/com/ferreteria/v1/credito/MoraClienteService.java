package com.ferreteria.v1.credito;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MoraClienteService {

    public EstadoMora calcularEstado(BigDecimal saldoPendiente, LocalDate vencimiento, LocalDate hoy) {
        if (saldoPendiente != null && saldoPendiente.signum() > 0) {
            if (hoy.isAfter(vencimiento)) {
                return EstadoMora.MOROSO;
            } else {
                if (!hoy.plusDays(3).isBefore(vencimiento)) {
                    return EstadoMora.POR_VENCER;
                } else {
                    return EstadoMora.AL_DIA;
                }
            }
        } else {
            return EstadoMora.AL_DIA;
        }
    }
}
