package com.ferreteria.v1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigo(String email, String nombre, String codigo, boolean recuperacion) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) message.setFrom(from);
        message.setTo(email);
        message.setSubject(recuperacion
                ? "Ferretería Charito - Recuperación de contraseña"
                : "Ferretería Charito - Código de verificación");
        message.setText(
                "Hola " + nombre + ",\n\n" +
                "Tu código de verificación es: " + codigo + "\n\n" +
                "El código vence en 10 minutos y solo puede utilizarse una vez.\n\n" +
                "Si no solicitaste esta operación, puedes ignorar este mensaje.\n\n" +
                "Ferretería Charito"
        );
        mailSender.send(message);
    }
}
