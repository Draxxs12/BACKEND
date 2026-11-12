package com.ferreteria.v1.services;

/** Puerto de salida para enviar correos de autenticación. */
public interface CodigoEmailSender {
    void enviar(String destinatario, String asunto, String cuerpo) throws Exception;
}
