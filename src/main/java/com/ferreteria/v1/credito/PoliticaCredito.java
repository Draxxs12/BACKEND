package com.ferreteria.v1.credito;

import java.time.LocalDate;

import com.ferreteria.v1.exceptions.ValidacionException;

public class PoliticaCredito {

    static final String SIN_FECHA   = "Indica la fecha de vencimiento del crédito.";
    static final String FECHA_PASADA = "La fecha de vencimiento debe ser posterior a hoy.";

    public void validarVencimiento(LocalDate vencimiento, LocalDate hoy) {
        if (vencimiento == null) {
            throw new ValidacionException(SIN_FECHA);
        }
        if (!vencimiento.isAfter(hoy)) {
            throw new ValidacionException(FECHA_PASADA);
        }
    }
}
