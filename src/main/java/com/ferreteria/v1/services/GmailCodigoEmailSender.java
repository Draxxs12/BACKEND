package com.ferreteria.v1.services;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/** Adaptador de producción para Gmail API mediante OAuth 2.0. */
@Component
@Profile("!test")
public class GmailCodigoEmailSender implements CodigoEmailSender {

    private static final String GMAIL_USER = "me";

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

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) throws Exception {
        if (clientId.isBlank() || clientSecret.isBlank() || refreshToken.isBlank() || senderEmail.isBlank()) {
            throw new IllegalStateException("Faltan GOOGLE_GMAIL_CLIENT_ID, GOOGLE_GMAIL_CLIENT_SECRET, "
                    + "GOOGLE_GMAIL_REFRESH_TOKEN o GOOGLE_GMAIL_SENDER_EMAIL");
        }

        Gmail gmailClient = obtenerGmail();
        MimeMessage mimeMessage = crearMensaje(senderEmail, destinatario, asunto, cuerpo);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);

        String encodedMessage = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(buffer.toByteArray());

        Message message = new Message().setRaw(encodedMessage);
        gmailClient.users().messages().send(GMAIL_USER, message).execute();
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
