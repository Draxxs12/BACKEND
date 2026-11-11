package com.ferreteria.v1.services;

import org.springframework.stereotype.Service;

/**
 * Construye los correos de autenticación y delega su entrega al proveedor configurado.
 * El servicio no depende de Gmail, por lo que los entornos de prueba pueden simular el envío.
 */
@Service
public class EmailService {

    private final CodigoEmailSender emailSender;

    public EmailService(CodigoEmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void enviarCodigo(String email, String nombre, String codigo, boolean recuperacion) {
        String subject = recuperacion
                ? "Ferretería Charito - Recuperación de contraseña"
                : "Ferretería Charito - Código de verificación";

        String body = "Hola " + nombre + ",\n\n"
                + (recuperacion
                    ? "Recibimos una solicitud para recuperar tu contraseña.\n\n"
                    : "Para completar tu inicio de sesión, utiliza el siguiente código:\n\n")
                + "Código: " + codigo + "\n\n"
                + "Este código vence en 10 minutos y solo puede utilizarse una vez.\n\n"
                + "Si no solicitaste esta operación, puedes ignorar este mensaje.\n\n"
                + "Ferretería Charito";

        try {
            emailSender.enviar(email, subject, body);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo enviar el correo: " + e.getMessage(), e);
        }
    }
}
