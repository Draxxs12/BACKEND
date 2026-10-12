package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.repositories.ProductoRepository;

/** CP-012, CP-074, CP-075, CP-078, CP-099: stock crítico, inactivar y stock 0. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductoServiceExtraTest {

    @Mock ProductoRepository productoRepository;
    @Mock UsuarioActualService usuarioActual;
    @InjectMocks ProductoService service;

    private Producto producto(int id, Integer stock, Integer stockMinimo) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Producto " + id);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setPrecioCompra(10.0);
        p.setPrecioVenta(15.0);
        p.setActivo(true);
        return p;
    }

    // ───────────── CP-012 / CP-099: stock crítico ─────────────
    @Test
    void productosConStockBajo_stockCeroEsCritico() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto(1, 0, 5)));
        assertEquals(1, service.productosConStockBajo().size());
    }

    @Test
    void productosConStockBajo_stockSobreElMinimoNoEsCritico() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto(1, 6, 5)));
        assertTrue(service.productosConStockBajo().isEmpty());
    }

    @Test
    void productosConStockBajo_ignoraProductosSinStockOSinMinimoDefinido() {
        when(productoRepository.findByActivoTrue())
                .thenReturn(List.of(producto(1, null, 5), producto(2, 3, null)));
        assertTrue(service.productosConStockBajo().isEmpty());
    }

    // ───────────── CP-078: stock 0 es válido al guardar ─────────────
    @Test
    void guardar_stockCeroEsValido_valorLimite() {
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
        Producto r = service.guardar(producto(1, 0, 5));
        assertEquals(0, r.getStock());
    }

    // ───────────── CP-074 / CP-075: inactivar sin borrar ─────────────
    @Test
    void eliminar_inactivaElProductoSinBorrarlo() {
        Producto p = producto(1, 10, 5);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));

        service.eliminar(1);

        assertFalse(p.getActivo());
        verify(productoRepository).save(p);
        verify(productoRepository, never()).deleteById(any());
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void eliminar_productoInexistenteNoFallaNiGuardaNada() {
        when(productoRepository.findById(404)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.eliminar(404));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void buscarPorId_inexistenteDevuelveNull() {
        when(productoRepository.findById(404)).thenReturn(Optional.empty());
        assertNull(service.buscarPorId(404));
    }
}
