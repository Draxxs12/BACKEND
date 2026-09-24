package com.ferreteria.v1.services;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.oauth2.UserCredentials;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final String GMAIL_USER = "me";

    // Se reutilizan las credenciales y el cliente Gmail durante la vida del backend.
    // Antes se reconstruían en cada correo y eso obligaba a repetir el flujo OAuth.
    private volatile UserCredentials credentials;
    private volatile Gmail gmail;
    
    @Value("${google.gmail.client-id:}")
    private String clientId;

    @Value("${google.gmail.client-secret:}")
    private String clientSecret;

    @Value("${google.gmail.refresh-token:}")
    private String refreshToken;

    @Value("${google.gmail.sender-email:}")
    private String senderEmail;

    @Value("${google.application-name:Ferretería Charito}")
    private String applicationName;

    public void enviarCodigo(String email, String nombre, String codigo, boolean recuperacion) {
        try {
            if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank() || senderEmail.isBlank()) {
                throw new IllegalStateException("Faltan GOOGLE_GMAIL_CLIENT_ID, GOOGLE_GMAIL_CLIENT_SECRET, "
                        + "GOOGLE_GMAIL_REFRESH_TOKEN o GOOGLE_GMAIL_SENDER_EMAIL");
            }

            Gmail gmailClient = obtenerGmail();
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

            MimeMessage mimeMessage = crearMensaje(senderEmail, email, subject, body);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);

            String encodedMessage = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(buffer.toByteArray());

            Message message = new Message().setRaw(encodedMessage);
            gmailClient.users().messages().send(GMAIL_USER, message).execute();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo enviar el correo mediante Gmail API: " + e.getMessage(), e);
        }
    }

    private Gmail obtenerGmail() throws Exception {
        UserCredentials creds = credentials;

        if (creds == null) {
            synchronized (this) {
                creds = credentials;
                if (creds == null) {
                    creds = UserCredentials.newBuilder()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret)
                            .setRefreshToken(refreshToken)
                            .build();

                    gmail = new Gmail.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            GsonFactory.getDefaultInstance(),
                            new HttpCredentialsAdapter(creds))
                            .setApplicationName(applicationName)
                            .build();

                    credentials = creds;
                }
            }
        }

        // Solo contacta a Google cuando el access token no existe o ya venció.
        synchronized (creds) {
            if (creds.getAccessToken() == null || creds.getAccessToken().getExpirationTime() == null
                    || creds.getAccessToken().getExpirationTime().getTime() <= System.currentTimeMillis() + 60_000) {
                creds.refreshAccessToken();
            }
        }

        return gmail;
    }

    private MimeMessage crearMensaje(String from, String to, String subject, String body) throws Exception {
        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        return message;
    }
}
