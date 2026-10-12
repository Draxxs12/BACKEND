package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.DetalleDevolucion;
import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Devolucion;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.DetalleVentaRepository;
import com.ferreteria.v1.repositories.DevolucionRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;

/** CP-052, CP-054, CP-058, CP-060 y CP-062: valor límite, parcial, vínculo con la venta y producto ajeno. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DevolucionServiceVinculoTest {

    @Mock DevolucionRepository devolucionRepository;
    @Mock VentaRepository ventaRepository;
    @Mock ProductoRepository productoRepository;
    @Mock InventarioRepository inventarioRepository;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @InjectMocks DevolucionService service;

    private Venta venta;

    @BeforeEach
    void preparar() {
        venta = new Venta();
        venta.setId(1);
        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));
        when(devolucionRepository.count()).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setStock(stock);
        when(productoRepository.findById(id)).thenReturn(Optional.of(p));
        return p;
    }

    private DetalleVenta vendido(Producto p, int cantidad) {
        DetalleVenta dv = new DetalleVenta();
        dv.setVenta(venta);
        dv.setProducto(p);
        dv.setCantidad(cantidad);
        return dv;
    }

    private DetalleDevolucion linea(Producto p, int cantidad, String precio) {
        DetalleDevolucion d = new DetalleDevolucion();
        d.setProducto(p);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal(precio));
        return d;
    }

    @Test
    void registrar_devolverExactamenteLoVendidoSeAcepta_valorLimite() {
        Producto p = producto(5, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(p, 5)));

        service.registrar(1, "Defecto", null, new ArrayList<>(List.of(linea(p, 5, "10.00"))), new Usuario());

        assertEquals(15, p.getStock());
    }

    @Test
    void registrar_unaUnidadMasDeLoVendidoSeRechaza_valorLimite() {
        Producto p = producto(5, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(p, 5)));

        assertThrows(ValidacionException.class, () -> service.registrar(1, "Defecto", null,
                new ArrayList<>(List.of(linea(p, 6, "10.00"))), new Usuario()));
        assertEquals(10, p.getStock());
    }

    @Test
    void registrar_devolucionParcialSumaSoloLoDevuelto() {
        Producto p = producto(5, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(p, 5)));

        Devolucion d = service.registrar(1, "Cambio", null,
                new ArrayList<>(List.of(linea(p, 2, "10.00"))), new Usuario());

        assertEquals(12, p.getStock());
        assertEquals(0, new BigDecimal("20.00").compareTo(d.getMontoReembolso()));
    }

    @Test
    void registrar_variasLineasSumanElMontoDeLaNotaDeCredito() {
        Producto a = producto(5, 10);
        Producto b = producto(6, 4);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(a, 3), vendido(b, 2)));

        Devolucion d = service.registrar(1, "Cambio", null, new ArrayList<>(List.of(
                linea(a, 2, "25.00"), linea(b, 1, "10.50"))), new Usuario());

        assertEquals(0, new BigDecimal("60.50").compareTo(d.getMontoReembolso()));   // 50.00 + 10.50
    }

    @Test
    void registrar_quedaVinculadaALaVentaOriginal() {
        Producto p = producto(5, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(p, 5)));

        Devolucion d = service.registrar(1, "Cambio", null,
                new ArrayList<>(List.of(linea(p, 1, "10.00"))), new Usuario());

        assertSame(venta, d.getVenta());
    }

    @Test
    void registrar_rechazaUnProductoQueNoEstaEnLaVentaOriginal() {
        Producto vendidoEnLaVenta = producto(5, 10);
        Producto ajeno = producto(9, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(vendidoEnLaVenta, 5)));

        assertThrows(ValidacionException.class, () -> service.registrar(1, "Cambio", null,
                new ArrayList<>(List.of(linea(ajeno, 1, "10.00"))), new Usuario()));
        assertEquals(10, ajeno.getStock());
    }

    @Test
    void registrar_unaLineaInvalidaNoModificaElStockDeLasLineasValidas() {
        Producto a = producto(5, 10);
        Producto b = producto(6, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(a, 3), vendido(b, 1)));

        assertThrows(ValidacionException.class, () -> service.registrar(1, "Cambio", null,
                new ArrayList<>(List.of(linea(a, 2, "10.00"), linea(b, 99, "10.00"))), new Usuario()));

        assertEquals(10, a.getStock());
        verify(productoRepository, never()).save(any());
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void registrar_mismoProductoEnDosLineasSeSumaParaValidar() {
        Producto p = producto(5, 10);
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(vendido(p, 5)));

        assertThrows(ValidacionException.class, () -> service.registrar(1, "Cambio", null,
                new ArrayList<>(List.of(linea(p, 3, "10.00"), linea(p, 3, "10.00"))), new Usuario()));
    }
}
