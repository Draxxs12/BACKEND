package com.ferreteria.v1.credito;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MoraClienteService {

    public EstadoMora calcularEstado(BigDecimal saldoPendiente, LocalDate vencimiento, LocalDate hoy) {
        if (saldoPendiente.signum() > 0 && hoy.isAfter(vencimiento)) {
            return EstadoMora.MOROSO;
        }
        return EstadoMora.AL_DIA;
    }
}
