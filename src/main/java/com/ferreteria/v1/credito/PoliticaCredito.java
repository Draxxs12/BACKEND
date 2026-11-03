package com.ferreteria.v1.credito;

import java.time.LocalDate;

import com.ferreteria.v1.exceptions.ValidacionException;

public class PoliticaCredito {

    public void validarVencimiento(LocalDate vencimiento, LocalDate hoy) {
        if (vencimiento == null) {
            throw new ValidacionException("Indica la fecha de vencimiento del crédito.");
        }
    }
}
