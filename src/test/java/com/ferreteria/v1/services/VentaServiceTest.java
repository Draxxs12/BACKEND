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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Caja;
import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.CajaRepository;
import com.ferreteria.v1.repositories.DetalleVentaRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;

/** H0029 a H0035 (venta y comprobante) y anulación; integración Ventas-Inventario (CP-021 a CP-034, CP-095). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VentaServiceTest {

    @Mock VentaRepository ventaRepository;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @Mock ProductoRepository productoRepository;
    @Mock InventarioRepository inventarioRepository;
    @Mock CajaRepository cajaRepository;
    @InjectMocks VentaService service;

    @BeforeEach
    void preparar() {
        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenAnswer(i -> i.getArgument(0));
        when(ventaRepository.count()).thenReturn(0L);
    }

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Producto " + id);
        p.setStock(stock);
        when(productoRepository.findById(id)).thenReturn(Optional.of(p));
        return p;
    }

    private DetalleVenta linea(Producto p, int cantidad, double precio) {
        DetalleVenta d = new DetalleVenta();
        d.setProducto(p);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precio);
        return d;
    }

    private Venta venta(Venta.TipoComprobante tipo) {
        Venta v = new Venta();
        v.setTipoComprobante(tipo);
        return v;
    }

    // ───────────── CP-032 / CP-033: venta correcta, total y stock ─────────────
    @Test
    void registrarVenta_calculaSubtotalIgvYTotal() {
        Producto p = producto(5, 10);
        Venta v = service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 2, 25.0))), new Usuario());

        assertEquals(50.0, v.getSubtotal(), 0.001);
        assertEquals(9.0, v.getIgv(), 0.001);          // 18 %
        assertEquals(59.0, v.getTotal(), 0.001);
    }

    @Test
    void registrarVenta_stockFinalEsStockInicialMenosCantidad_integracion() {
        Producto p = producto(5, 10);
        service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 4, 10.0))), new Usuario());

        assertEquals(6, p.getStock());                  // 10 - 4
        verify(productoRepository).save(p);
    }

    @Test
    void registrarVenta_registraMovimientoDeSalidaEnElInventario_integracion() {
        Producto p = producto(5, 10);
        service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 3, 10.0))), new Usuario());

        ArgumentCaptor<Inventario> cap = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(cap.capture());
        Inventario mov = cap.getValue();
        assertEquals(Inventario.TipoMovimiento.Salida, mov.getTipo());
        assertEquals(3, mov.getCantidad());
        assertEquals(10, mov.getStockAntes());
        assertEquals(7, mov.getStockDespues());
        assertTrue(mov.getMotivo().contains("B001-00001"));
    }

    @Test
    void registrarVenta_asignaElUsuarioQueVende() {
        Producto p = producto(5, 10);
        Usuario u = new Usuario();
        Venta v = service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 1, 10.0))), u);
        assertSame(u, v.getUsuario());
    }

    // ───────────── CP-023 / CP-034: valores límite de stock ─────────────
    @Test
    void registrarVenta_vender_exactamenteElStockDisponibleEsValido_valorLimite() {
        Producto p = producto(5, 10);
        service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 10, 10.0))), new Usuario());
        assertEquals(0, p.getStock());
    }

    @Test
    void registrarVenta_rechazaCantidadMayorAlStock_yNoCambiaElStock() {
        Producto p = producto(5, 10);

        assertThrows(RuntimeException.class, () -> service.registrarVenta(
                venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 11, 10.0))), new Usuario()));

        assertEquals(10, p.getStock());
        verify(productoRepository, never()).save(any());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void registrarVenta_productoInexistenteLanzaExcepcion() {
        Producto fantasma = new Producto();
        fantasma.setId(999);
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.registrarVenta(
                venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(fantasma, 1, 10.0))), new Usuario()));
    }

    // ───────────── CP-024: cantidad 0 o negativa (se espera ROJO con el código actual) ─────────────
    @Test
    void registrarVenta_rechazaCantidadCero() {
        Producto p = producto(5, 10);
        assertThrows(ValidacionException.class, () -> service.registrarVenta(
                venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 0, 10.0))), new Usuario()));
    }

    @Test
    void registrarVenta_rechazaCantidadNegativa_yNoSumaStock() {
        Producto p = producto(5, 10);
        assertThrows(ValidacionException.class, () -> service.registrarVenta(
                venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, -3, 10.0))), new Usuario()));
        assertEquals(10, p.getStock());
    }

    // ───────────── H0031: serie y número de comprobante ─────────────
    @Test
    void registrarVenta_boletaUsaSerieB001Correlativa() {
        Producto p = producto(5, 10);
        Venta v = service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 1, 10.0))), new Usuario());
        assertEquals("B001-00001", v.getNumeroComprobante());
    }

    @Test
    void registrarVenta_facturaUsaSerieF001ConElSiguienteNumero() {
        when(ventaRepository.count()).thenReturn(4L);
        Producto p = producto(5, 10);
        Venta v = service.registrarVenta(venta(Venta.TipoComprobante.Factura),
                new ArrayList<>(List.of(linea(p, 1, 10.0))), new Usuario());
        assertEquals("F001-00005", v.getNumeroComprobante());
    }

    @Test
    void registrarVenta_respetaUnNumeroDeComprobanteYaAsignado() {
        Producto p = producto(5, 10);
        Venta base = venta(Venta.TipoComprobante.Boleta);
        base.setNumeroComprobante("B001-00099");
        Venta v = service.registrarVenta(base,
                new ArrayList<>(List.of(linea(p, 1, 10.0))), new Usuario());
        assertEquals("B001-00099", v.getNumeroComprobante());
    }

    // ───────────── Caja ─────────────
    @Test
    void registrarVenta_conCajaAbiertaSumaElTotalALaCaja() {
        Caja caja = new Caja();
        caja.setTotalVentas(new BigDecimal("100.00"));
        when(cajaRepository.findFirstByEstadoOrderByAperturaDesc(Caja.Estado.Abierta))
                .thenReturn(Optional.of(caja));
        Producto p = producto(5, 10);

        service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 2, 25.0))), new Usuario());

        assertEquals(0, new BigDecimal("159.00").compareTo(caja.getTotalVentas()));
        verify(cajaRepository).save(caja);
    }

    @Test
    void registrarVenta_sinCajaAbiertaNoFalla() {
        Producto p = producto(5, 10);
        assertDoesNotThrow(() -> service.registrarVenta(venta(Venta.TipoComprobante.Boleta),
                new ArrayList<>(List.of(linea(p, 1, 10.0))), new Usuario()));
        verify(cajaRepository, never()).save(any());
    }

    // ───────────── Anular venta (integración Ventas-Inventario) ─────────────
    @Test
    void anular_devuelveElStockRegistraEntradaYMarcaAnulada() {
        Producto p = new Producto();
        p.setId(5); p.setStock(8);
        Venta v = new Venta();
        v.setId(1);
        v.setNumeroComprobante("B001-00001");
        DetalleVenta d = linea(p, 2, 10.0);
        when(ventaRepository.findById(1)).thenReturn(Optional.of(v));
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(d));

        Venta r = service.anular(1, new Usuario());

        assertEquals(10, p.getStock());                 // 8 + 2
        assertEquals(Venta.EstadoVenta.Anulada, r.getEstado());
        ArgumentCaptor<Inventario> cap = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(cap.capture());
        assertEquals(Inventario.TipoMovimiento.Entrada, cap.getValue().getTipo());
        assertTrue(cap.getValue().getMotivo().contains("B001-00001"));
    }

    @Test
    void anular_unaVentaYaAnuladaSeRechazaYNoTocaElStock() {
        Venta v = new Venta();
        v.setId(1);
        v.setEstado(Venta.EstadoVenta.Anulada);
        when(ventaRepository.findById(1)).thenReturn(Optional.of(v));

        assertThrows(RuntimeException.class, () -> service.anular(1, new Usuario()));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void anular_ventaInexistenteLanzaExcepcion() {
        when(ventaRepository.findById(404)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.anular(404, new Usuario()));
    }

    // ───────────── H0035 / H0038: listar y detalle ─────────────
    @Test
    void listarTodas_devuelveLasVentasDelRepositorio() {
        when(ventaRepository.findAll()).thenReturn(List.of(new Venta(), new Venta()));
        assertEquals(2, service.listarTodas().size());
    }

    @Test
    void listarTodas_sinVentasDevuelveListaVaciaSinError() {
        when(ventaRepository.findAll()).thenReturn(List.of());
        assertTrue(service.listarTodas().isEmpty());
    }

    @Test
    void buscarPorId_inexistenteDevuelveNull() {
        when(ventaRepository.findById(404)).thenReturn(Optional.empty());
        assertNull(service.buscarPorId(404));
    }

    @Test
    void obtenerDetalle_delegaEnElRepositorioDeDetalles() {
        when(detalleVentaRepository.findByVentaId(1)).thenReturn(List.of(new DetalleVenta()));
        assertEquals(1, service.obtenerDetalle(1).size());
    }
}
