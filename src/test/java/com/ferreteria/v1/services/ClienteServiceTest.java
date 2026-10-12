package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Cliente;
import com.ferreteria.v1.repositories.ClienteRepository;

/** H0009 a H0012 (clientes, Sprint 1): CP-226 a CP-232. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks ClienteService service;

    @BeforeEach
    void preparar() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioActual.nombreActual()).thenReturn("Admin");
    }

    private Cliente cliente(Cliente.TipoDocumento tipo, String numero) {
        Cliente c = new Cliente();
        c.setNombre("Cliente de prueba");
        c.setTipoDocumento(tipo);
        c.setNumeroDocumento(numero);
        return c;
    }

    @Test
    void guardar_dniDe8DigitosSeRegistraConElUsuarioQueLoRegistra() {
        Cliente r = service.guardar(cliente(Cliente.TipoDocumento.DNI, "12345678"));
        assertEquals("12345678", r.getNumeroDocumento());
        assertEquals("Admin", r.getRegistradoPor());
    }

    @Test
    void guardar_dniDe7DigitosSeRechazaYNoSeGuarda_valorLimite() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(cliente(Cliente.TipoDocumento.DNI, "1234567")));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void guardar_dniDe9DigitosSeRechaza_valorLimite() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(cliente(Cliente.TipoDocumento.DNI, "123456789")));
    }

    @Test
    void guardar_rucDe11DigitosEsValido_valorLimite() {
        assertDoesNotThrow(() -> service.guardar(cliente(Cliente.TipoDocumento.RUC, "20123456789")));
    }

    @Test
    void guardar_rucDe10DigitosSeRechaza_valorLimite() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(cliente(Cliente.TipoDocumento.RUC, "2012345678")));
    }

    @Test
    void guardar_rucDe12DigitosSeRechaza_valorLimite() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(cliente(Cliente.TipoDocumento.RUC, "201234567890")));
    }

    @Test
    void guardar_documentoConLetrasSeRechaza() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(cliente(Cliente.TipoDocumento.DNI, "1234567A")));
    }

    @Test
    void guardar_sinDocumentoEsValido() {
        assertDoesNotThrow(() -> service.guardar(cliente(Cliente.TipoDocumento.DNI, null)));
    }

    @Test
    void guardar_correoConFormatoInvalidoSeRechaza() {
        Cliente c = cliente(Cliente.TipoDocumento.DNI, "12345678");
        c.setEmail("sin-arroba");
        assertThrows(ValidacionException.class, () -> service.guardar(c));
    }

    @Test
    void listarTodos_sinClientesDevuelveListaVaciaSinError() {
        when(clienteRepository.findAll()).thenReturn(List.of());
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void buscarPorNombre_delegaEnLaBusquedaDelRepositorio() {
        when(clienteRepository.findByNombreContainingIgnoreCase("ana")).thenReturn(List.of(new Cliente()));
        assertEquals(1, service.buscarPorNombre("ana").size());
    }
}
