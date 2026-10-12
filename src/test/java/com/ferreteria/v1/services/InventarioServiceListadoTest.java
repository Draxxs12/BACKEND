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

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;

/** HU-S2-21 Listar stock actual / H0059 Registrar movimientos de stock: lecturas y casos de borde del ajuste. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventarioServiceListadoTest {

    @Mock InventarioRepository inventarioRepository;
    @Mock ProductoRepository productoRepository;
    @InjectMocks InventarioService service;

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Producto " + id);
        p.setStock(stock);
        return p;
    }

    private Inventario movimiento(Producto p, Inventario.TipoMovimiento tipo) {
        Inventario m = new Inventario();
        m.setProducto(p);
        m.setTipo(tipo);
        return m;
    }

    // ───────────── Historial de movimientos (alimenta la pestaña "Movimientos") ─────────────
    @Test
    void listarTodos_devuelveTodosLosMovimientosDelRepositorio() {
        Producto p = producto(1, 10);
        when(inventarioRepository.findAll()).thenReturn(List.of(
                movimiento(p, Inventario.TipoMovimiento.Entrada),
                movimiento(p, Inventario.TipoMovimiento.Salida),
                movimiento(p, Inventario.TipoMovimiento.Ajuste)));

        assertEquals(3, service.listarTodos().size());
    }

    @Test
    void listarTodos_sinMovimientosDevuelveListaVaciaSinError() {
        when(inventarioRepository.findAll()).thenReturn(List.of());

        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void listarPorProducto_pideSoloLosMovimientosDeEseProducto() {
        Producto p = producto(4, 10);
        when(inventarioRepository.findByProductoId(4)).thenReturn(List.of(movimiento(p, Inventario.TipoMovimiento.Entrada)));

        List<Inventario> r = service.listarPorProducto(4);

        assertEquals(1, r.size());
        verify(inventarioRepository).findByProductoId(4);
        verify(inventarioRepository, never()).findAll();
    }

    @Test
    void listarPorProducto_sinMovimientosDevuelveListaVacia() {
        when(inventarioRepository.findByProductoId(8)).thenReturn(List.of());

        assertTrue(service.listarPorProducto(8).isEmpty());
    }

    @Test
    void guardar_persisteElMovimientoTalCual() {
        Inventario m = movimiento(producto(1, 5), Inventario.TipoMovimiento.Entrada);
        when(inventarioRepository.save(m)).thenReturn(m);

        assertSame(m, service.guardar(m));
        verify(inventarioRepository).save(m);
    }

    // ───────────── Ajuste manual: casos de borde ─────────────
    @Test
    void ajustarStock_alMismoValorRegistraCantidadCero_valorLimite() {
        Producto p = producto(1, 10);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario mov = service.ajustarStock(1, 10, "Conteo coincide", new Usuario());

        assertEquals(10, p.getStock());
        assertEquals(0, mov.getCantidad());
        assertEquals(10, mov.getStockAntes());
        assertEquals(10, mov.getStockDespues());
    }

    @Test
    void ajustarStock_unaUnidadMenosRegistraCantidadUno_valorLimite() {
        Producto p = producto(1, 10);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario mov = service.ajustarStock(1, 9, "Rotura", new Usuario());

        assertEquals(9, p.getStock());
        assertEquals(1, mov.getCantidad());
    }

    @Test
    void ajustarStock_menosUnoSeRechaza_valorLimite() {
        Producto p = producto(1, 10);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));

        assertThrows(ValidacionException.class, () -> service.ajustarStock(1, -1, "x", new Usuario()));

        assertEquals(10, p.getStock());
        verify(productoRepository, never()).save(any());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void ajustarStock_sinIndicarElNuevoStockSeRechaza() {
        assertThrows(ValidacionException.class, () -> service.ajustarStock(1, null, "x", new Usuario()));

        verify(productoRepository, never()).findById(any());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void ajustarStock_noTocaElStockDeOtrosProductos() {
        Producto objetivo = producto(1, 10);
        Producto otro = producto(2, 50);
        when(productoRepository.findById(1)).thenReturn(Optional.of(objetivo));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        service.ajustarStock(1, 25, "Conteo", new Usuario());

        assertEquals(25, objetivo.getStock());
        assertEquals(50, otro.getStock());
        verify(productoRepository, never()).findById(2);
    }

    @Test
    void ajustarStock_elMotivoEscritoSeConservaTalCual() {
        Producto p = producto(1, 10);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario mov = service.ajustarStock(1, 12, "Inventario anual 2026", new Usuario());

        assertEquals("Inventario anual 2026", mov.getMotivo());
    }
}
