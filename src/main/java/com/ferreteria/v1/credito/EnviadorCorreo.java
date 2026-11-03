package com.ferreteria.v1.credito;

/** Puerto de salida del correo. EmailService (Gmail API) podra implementarlo mas adelante. */
public interface EnviadorCorreo {
    void enviar(String destino, String asunto, String cuerpo);
}
