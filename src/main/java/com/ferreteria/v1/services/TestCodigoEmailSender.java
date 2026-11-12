package com.ferreteria.v1.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adaptador sin efectos externos para pruebas y escenarios de carga.
 * No envía ni registra códigos MFA reales.
 */
@Component
@Profile("test")
public class TestCodigoEmailSender implements CodigoEmailSender {

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        // Simula una entrega inmediata sin contactar a Gmail.
    }
}
