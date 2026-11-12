package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private CodigoEmailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void enviarCodigo_login_delegaAlProveedorConContenidoDeVerificacion() throws Exception {
        assertDoesNotThrow(() -> emailService.enviarCodigo(
                "cajero@ferreteria.test", "Cajero", "123456", false));

        verify(emailSender).enviar(
                eq("cajero@ferreteria.test"),
                eq("Ferretería Charito - Código de verificación"),
                contains("Código: 123456"));
    }

    @Test
    void enviarCodigo_recuperacion_usaAsuntoDeRecuperacion() throws Exception {
        emailService.enviarCodigo("cliente@ferreteria.test", "Cliente", "654321", true);

        verify(emailSender).enviar(
                eq("cliente@ferreteria.test"),
                eq("Ferretería Charito - Recuperación de contraseña"),
                contains("recuperar tu contraseña"));
    }

    @Test
    void enviarCodigo_siProveedorFalla_propagaErrorDeServicio() throws Exception {
        doThrow(new IllegalStateException("proveedor no disponible"))
                .when(emailSender).enviar(eq("cajero@ferreteria.test"), eq("Ferretería Charito - Código de verificación"), contains("Código: 123456"));

        assertThrows(IllegalStateException.class, () -> emailService.enviarCodigo(
                "cajero@ferreteria.test", "Cajero", "123456", false));
    }
}
