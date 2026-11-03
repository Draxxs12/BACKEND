package com.ferreteria.v1.credito;

import java.math.BigDecimal;

public class NotificadorPago {

    private final EnviadorCorreo correo;

    public NotificadorPago(EnviadorCorreo correo) {
        this.correo = correo;
    }

    public ResultadoEnvio confirmarPago(String emailCliente, String factura, BigDecimal monto) {
        if (!tieneCorreo(emailCliente)) {
            return ResultadoEnvio.SIN_CORREO;
        }
        try {
            correo.enviar(emailCliente, asunto(factura), cuerpo(factura, monto));
            return ResultadoEnvio.ENVIADO;
        } catch (RuntimeException e) {
            return ResultadoEnvio.PENDIENTE;   // el pago ya esta guardado; el envio se reintenta despues
        }
    }

    private boolean tieneCorreo(String email) {
        return email != null && !email.isBlank();
    }

    private String asunto(String factura) {
        return "Pago recibido " + factura;
    }

    private String cuerpo(String factura, BigDecimal monto) {
        return "Hemos registrado su pago de S/ " + monto + " de la factura " + factura + ".";
    }
}
