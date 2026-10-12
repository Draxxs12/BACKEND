package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Rol;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.RolRepository;
import com.ferreteria.v1.repositories.UsuarioRepository;

/** H0005 a H0008 (usuarios, Sprint 1): CP-215 a CP-225. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock RolRepository rolRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks UsuarioService service;

    @BeforeEach
    void preparar() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenAnswer(i -> "HASH(" + i.getArgument(0) + ")");
        when(usuarioActual.nombreActual()).thenReturn("Admin");
    }

    private Usuario existente() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setNombre("Ana");
        u.setEmail("ana@mail.com");
        u.setPasswordHash("HASH(vieja)");
        u.setActivo(true);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        return u;
    }

    // ───────────── CP-215 / CP-218 ─────────────
    @Test
    void crear_guardaElUsuarioActivoYConElRolIndicado() {
        Rol rol = new Rol();
        when(rolRepository.findById(2)).thenReturn(Optional.of(rol));

        Usuario u = service.crear("Ana", "ana@mail.com", "secreta", 2, null);

        assertTrue(u.getActivo());
        assertSame(rol, u.getRol());
        assertEquals("Admin", u.getRegistradoPor());
    }

    @Test
    void crear_laContrasenaNoSeGuardaEnTextoPlano() {
        Usuario u = service.crear("Ana", "ana@mail.com", "secreta", null, true);
        assertNotEquals("secreta", u.getPasswordHash());
        assertEquals("HASH(secreta)", u.getPasswordHash());
    }

    // ───────────── CP-217: datos inválidos ─────────────
    @Test
    void crear_correoInvalidoSeRechazaYNoSeGuarda() {
        assertThrows(ValidacionException.class,
                () -> service.crear("Ana", "sin-arroba", "secreta", null, true));
        verify(usuarioRepository, never()).save(any());
    }

    // ───────────── CP-221 / CP-222 ─────────────
    @Test
    void actualizar_sinContrasenaConservaElHashActual() {
        Usuario u = existente();
        Usuario r = service.actualizar(1, "Ana María", null, null, null, null);
        assertEquals("Ana María", r.getNombre());
        assertEquals("HASH(vieja)", r.getPasswordHash());
        assertSame(u, r);
    }

    @Test
    void actualizar_conContrasenaEnBlancoConservaElHashActual() {
        existente();
        Usuario r = service.actualizar(1, null, null, "   ", null, null);
        assertEquals("HASH(vieja)", r.getPasswordHash());
    }

    @Test
    void actualizar_conContrasenaNuevaCambiaElHash() {
        existente();
        Usuario r = service.actualizar(1, null, null, "nueva", null, null);
        assertEquals("HASH(nueva)", r.getPasswordHash());
    }

    @Test
    void actualizar_correoInvalidoSeRechaza() {
        existente();
        assertThrows(ValidacionException.class,
                () -> service.actualizar(1, null, "sin-arroba", null, null, null));
    }

    @Test
    void actualizar_usuarioInexistenteLanzaExcepcion() {
        when(usuarioRepository.findById(404)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.actualizar(404, "X", null, null, null, null));
    }

    // ───────────── CP-223: inactivar sin borrar ─────────────
    @Test
    void eliminar_inactivaAlUsuarioSinBorrarlo() {
        Usuario u = existente();
        service.eliminar(1);
        assertFalse(u.getActivo());
        verify(usuarioRepository).save(u);
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_usuarioInexistenteNoFallaNiGuardaNada() {
        when(usuarioRepository.findById(404)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.eliminar(404));
        verify(usuarioRepository, never()).save(any());
    }

    // ───────────── CP-219 / CP-220 ─────────────
    @Test
    void listarActivos_pideSoloLosActivos() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(new Usuario()));
        assertEquals(1, service.listarActivos().size());
        verify(usuarioRepository).findByActivoTrue();
    }

    @Test
    void buscarPorEmail_inexistenteDevuelveNull() {
        when(usuarioRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());
        assertNull(service.buscarPorEmail("nadie@mail.com"));
    }
}
