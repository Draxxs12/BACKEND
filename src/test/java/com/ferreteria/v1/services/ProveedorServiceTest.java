package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Proveedor;
import com.ferreteria.v1.repositories.ProveedorRepository;

/** H0013 a H0016 (proveedores, Sprint 1): CP-237 a CP-246. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProveedorServiceTest {

    @Mock ProveedorRepository proveedorRepository;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks ProveedorService service;

    @BeforeEach
    void preparar() {
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioActual.nombreActual()).thenReturn("Admin");
    }

    private Proveedor proveedor(String ruc) {
        Proveedor p = new Proveedor();
        p.setEmpresa("Aceros SAC");
        p.setRuc(ruc);
        return p;
    }

    @Test
    void guardar_rucDe11DigitosSeRegistraConElUsuarioQueLoRegistra() {
        Proveedor r = service.guardar(proveedor("20123456789"));
        assertEquals("20123456789", r.getRuc());
        assertEquals("Admin", r.getRegistradoPor());
    }

    @Test
    void guardar_rucConLetrasSeRechaza() {
        assertThrows(ValidacionException.class, () -> service.guardar(proveedor("2012345678A")));
    }

    @Test
    void guardar_correoConFormatoInvalidoSeRechaza() {
        Proveedor p = proveedor("20123456789");
        p.setEmail("sin-arroba");
        assertThrows(ValidacionException.class, () -> service.guardar(p));
    }

    // ───────────── CP-238: RUC de 10 y 12 dígitos (se espera ROJO: Validador.ruc admite de 1 a 20 dígitos) ─────────────
    @Test
    void guardar_rucDe10DigitosSeRechaza_valorLimite() {
        assertThrows(ValidacionException.class, () -> service.guardar(proveedor("2012345678")));
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void guardar_rucDe12DigitosSeRechaza_valorLimite() {
        assertThrows(ValidacionException.class, () -> service.guardar(proveedor("201234567890")));
    }

    // ───────────── CP-240: nombre vacío (se espera ROJO: no se valida la empresa) ─────────────
    @Test
    void guardar_empresaVaciaSeRechaza() {
        Proveedor p = proveedor("20123456789");
        p.setEmpresa("  ");
        assertThrows(ValidacionException.class, () -> service.guardar(p));
    }

    // ───────────── CP-244 / CP-246: inactivar conserva el registro (se espera ROJO: eliminar() borra) ─────────────
    @Test
    void eliminar_inactivaAlProveedorSinBorrarloParaConservarSusCompras() {
        Proveedor p = proveedor("20123456789");
        p.setId(1);
        when(proveedorRepository.findById(1)).thenReturn(Optional.of(p));

        service.eliminar(1);

        assertFalse(p.getActivo());
        verify(proveedorRepository, never()).deleteById(any());
    }
}
