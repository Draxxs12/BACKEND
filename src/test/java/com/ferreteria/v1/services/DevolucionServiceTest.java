package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.models.DetalleDevolucion;
import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Devolucion;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.DetalleVentaRepository;
import com.ferreteria.v1.repositories.DevolucionRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;

/** HU-S2-13 (registrar devolución) y HU-S2-14 (listar devoluciones). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DevolucionServiceTest {

    @Mock DevolucionRepository devolucionRepository;
    @Mock VentaRepository ventaRepository;
    @Mock ProductoRepository productoRepository;
    @Mock InventarioRepository inventarioRepository;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @InjectMocks DevolucionService service;

    @org.junit.jupiter.api.BeforeEach
    void prepararAcumulado() {
        when(devolucionRepository.cantidadDevueltaPorVentaYProducto(anyInt(), anyInt())).thenReturn(0L);
    }

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setStock(stock);
        return p;
    }

    private Venta venta(int id) {
        Venta v = new Venta();
        v.setId(id);
        return v;
    }

    private DetalleDevolucion linea(Producto p, int cantidad, String precio) {
        DetalleDevolucion d = new DetalleDevolucion();
        d.setProducto(p);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal(precio));
        return d;
    }

    private DetalleVenta vendido(Venta v, Producto p, int cantidad) {
        DetalleVenta dv = new DetalleVenta();
        dv.setVenta(v);
        dv.setProducto(p);
        dv.setCantidad(cantidad);
        return dv;
    }

    // ───────────── HU-S2-13 ─────────────
    @Test
    void registrar_ventaInexistenteLanzaExcepcionYNoTocaNada() {
        when(ventaRepository.buscarPorIdParaActualizar(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.registrar(999, "Defecto", null, new ArrayList<>(), new Usuario()));

        verify(devolucionRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void registrar_devolucionValidaAumentaStockCalculaMontoYRegistraEntrada() {
        Producto p = producto(5, 10);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.count()).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 5)));

        List<DetalleDevolucion> detalles = new ArrayList<>(List.of(linea(p, 2, "25.00")));
        Devolucion dev = service.registrar(1, "Defecto de fábrica", Devolucion.TipoReembolso.Tarjeta,
                detalles, new Usuario());

        assertEquals(12, p.getStock());                                   // 10 + 2
        assertEquals(0, new BigDecimal("50.00").compareTo(dev.getMontoReembolso()));
        assertEquals("NC-00001", dev.getNumeroNota());
        assertEquals(Devolucion.TipoReembolso.Tarjeta, dev.getTipoReembolso());

        ArgumentCaptor<Inventario> cap = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(cap.capture());
        Inventario mov = cap.getValue();
        assertEquals(Inventario.TipoMovimiento.Entrada, mov.getTipo());
        assertEquals(2, mov.getCantidad());
        assertEquals(10, mov.getStockAntes());
        assertEquals(12, mov.getStockDespues());
        assertTrue(mov.getMotivo().contains("NC-00001"));
    }

    @Test
    void registrar_sinTipoDeReembolsoUsaEfectivo() {
        Producto p = producto(5, 10);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.count()).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 5)));

        Devolucion dev = service.registrar(1, "x", null,
                new ArrayList<>(List.of(linea(p, 1, "10.00"))), new Usuario());

        assertEquals(Devolucion.TipoReembolso.Efectivo, dev.getTipoReembolso());
    }

    @Test
    void registrar_elNumeroDeNotaEsCorrelativo() {
        Producto p = producto(5, 10);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.count()).thenReturn(4L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 5)));

        Devolucion dev = service.registrar(1, "x", null,
                new ArrayList<>(List.of(linea(p, 1, "10.00"))), new Usuario());

        assertEquals("NC-00005", dev.getNumeroNota());
    }

    /** Valida que una devolución nunca tenga cantidades cero o negativas. */
    @Test
    void registrar_rechazaCantidadNegativaOCero() {
        Producto p = producto(5, 10);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.count()).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 5)));

        assertThrows(RuntimeException.class, () -> service.registrar(1, "x", null,
                new ArrayList<>(List.of(linea(p, -3, "10.00"))), new Usuario()));
        assertThrows(RuntimeException.class, () -> service.registrar(1, "x", null,
                new ArrayList<>(List.of(linea(p, 0, "10.00"))), new Usuario()));

        assertEquals(10, p.getStock(), "el stock no debe cambiar");
    }

    /** Valida que la cantidad solicitada no supere lo vendido. */
    @Test
    void registrar_rechazaDevolverMasDeLoVendido() {
        Producto p = producto(5, 10);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.count()).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 2)));

        assertThrows(RuntimeException.class, () -> service.registrar(1, "x", null,
                new ArrayList<>(List.of(linea(p, 3, "10.00"))), new Usuario()));

        assertEquals(10, p.getStock(), "el stock no debe cambiar");
    }

    @Test
    void registrar_rechazaDevolucionesAcumuladasPorEncimaDeLoVendido() {
        Producto p = producto(5, 12);
        Venta v = venta(1);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));
        when(productoRepository.buscarPorIdParaActualizar(5)).thenReturn(Optional.of(p));
        when(devolucionRepository.cantidadDevueltaPorVentaYProducto(1, 5)).thenReturn(2L);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(v, p, 5)));

        assertThrows(com.ferreteria.v1.exceptions.ValidacionException.class, () ->
                service.registrar(1, "Segundo intento", null,
                        new ArrayList<>(List.of(linea(p, 4, "10.00"))), new Usuario()));

        assertEquals(12, p.getStock());
        verify(productoRepository, never()).save(any());
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    @Test
    void registrar_rechazaDevolucionDeUnaVentaAnulada() {
        Venta v = venta(1);
        v.setEstado(Venta.EstadoVenta.Anulada);
        when(ventaRepository.buscarPorIdParaActualizar(1)).thenReturn(Optional.of(v));

        assertThrows(com.ferreteria.v1.exceptions.ValidacionException.class, () ->
                service.registrar(1, "Venta anulada", null, new ArrayList<>(), new Usuario()));

        verify(productoRepository, never()).save(any());
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    // ───────────── HU-S2-14 ─────────────
    @Test
    void listarTodas_pideLasDevolucionesOrdenadasPorFechaDescendente() {
        Devolucion d = new Devolucion();
        when(devolucionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(d));

        assertEquals(1, service.listarTodas().size());
        verify(devolucionRepository).findAllByOrderByCreatedAtDesc();
    }
}
