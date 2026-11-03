package com.ferreteria.v1.credito;

import java.math.BigDecimal;

public class NotificadorPago {

    private final EnviadorCorreo correo;

    public NotificadorPago(EnviadorCorreo correo) {
        this.correo = correo;
    }

    public ResultadoEnvio confirmarPago(String emailCliente, String factura, BigDecimal monto) {
        if (emailCliente == null || emailCliente.isBlank()) {
            return ResultadoEnvio.SIN_CORREO;
        }
        try {
            correo.enviar(emailCliente, "Pago recibido " + factura,
                          "Hemos registrado su pago de S/ " + monto + " de la factura " + factura + ".");
            return ResultadoEnvio.ENVIADO;
        } catch (RuntimeException e) {
            return ResultadoEnvio.PENDIENTE;
        }
    }
}
