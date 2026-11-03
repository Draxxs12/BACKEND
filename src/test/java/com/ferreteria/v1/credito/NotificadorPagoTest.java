package com.ferreteria.v1.credito;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** HU-S3-04 (RF-17): correo de confirmacion de pago. El correo se simula con una interfaz (sin Gmail). */
class NotificadorPagoTest {

    private final List<String> enviados = new ArrayList<>();
    private final EnviadorCorreo correoOk = (destino, asunto, cuerpo) -> enviados.add(destino + "|" + asunto);
    private final BigDecimal monto = new BigDecimal("500.00");

    @Test
    void conCorreoDelClienteSeEnviaLaConfirmacionDePago() {
        NotificadorPago notificador = new NotificadorPago(correoOk);

        ResultadoEnvio r = notificador.confirmarPago("ventas@losandes.pe", "F001-00000045", monto);

        assertEquals(ResultadoEnvio.ENVIADO, r);
        assertEquals(1, enviados.size());
        assertTrue(enviados.get(0).startsWith("ventas@losandes.pe|"));
    }

    @Test
    void elAsuntoLlevaElNumeroDeFactura() {
        new NotificadorPago(correoOk).confirmarPago("ventas@losandes.pe", "F001-00000045", monto);

        assertTrue(enviados.get(0).contains("F001-00000045"));
    }

    @Test
    void siElServicioDeCorreoFallaElResultadoEsPendienteYNoSePropagaElError() {
        EnviadorCorreo caido = (destino, asunto, cuerpo) -> { throw new IllegalStateException("Gmail caído"); };

        ResultadoEnvio r = new NotificadorPago(caido).confirmarPago("ventas@losandes.pe", "F001-00000045", monto);

        assertEquals(ResultadoEnvio.PENDIENTE, r);
    }

    @Test
    void sinCorreoRegistradoNoSeEnviaNada() {
        NotificadorPago notificador = new NotificadorPago(correoOk);

        assertEquals(ResultadoEnvio.SIN_CORREO, notificador.confirmarPago("  ", "F001-00000045", monto));
        assertEquals(ResultadoEnvio.SIN_CORREO, notificador.confirmarPago(null, "F001-00000045", monto));
        assertTrue(enviados.isEmpty());
    }
}
