package com.ferreteria.v1.credito;

import java.math.BigDecimal;

/** Esqueleto (fase Red): compila, pero devuelve null y no envia nada. */
public class NotificadorPago {

    private final EnviadorCorreo correo;

    public NotificadorPago(EnviadorCorreo correo) {
        this.correo = correo;
    }

    public ResultadoEnvio confirmarPago(String emailCliente, String factura, BigDecimal monto) {
        return null;
    }
}
