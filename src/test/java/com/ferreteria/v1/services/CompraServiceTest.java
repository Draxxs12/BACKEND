package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.models.DetalleCompra;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Proveedor;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;

/** H0017 a H0019 (compras, Sprint 1): CP-247 a CP-256 y movimientos de stock (CP-095). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompraServiceTest {

    @Mock CompraRepository compraRepository;
    @Mock ProductoRepository productoRepository;
    @Mock InventarioRepository inventarioRepository;
    @InjectMocks CompraService service;

    @BeforeEach
    void preparar() {
        when(compraRepository.save(any(Compra.class))).thenAnswer(i -> i.getArgument(0));
        when(compraRepository.count()).thenReturn(0L);
    }

    private Producto producto(int id, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setStock(stock);
        when(productoRepository.findById(id)).thenReturn(Optional.of(p));
        return p;
    }

    private Compra compra(Compra.Estado estado, Producto p, int cantidad, String precio) {
        Compra c = new Compra();
        c.setProveedor(new Proveedor());
        c.setEstado(estado);
        DetalleCompra d = new DetalleCompra();
        d.setProducto(p);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal(precio));
        c.getDetalles().add(d);
        return c;
    }

    // ───────────── CP-247: guardar con su total ─────────────
    @Test
    void guardar_calculaSubtotalIgvYTotal() {
        Producto p = producto(5, 10);
        Compra r = service.guardar(compra(Compra.Estado.Pendiente, p, 5, "20.00"));

        assertEquals(0, new BigDecimal("100.00").compareTo(r.getSubtotal()));
        assertEquals(0, new BigDecimal("18.00").compareTo(r.getIgv()));
        assertEquals(0, new BigDecimal("118.00").compareTo(r.getTotal()));
    }

    @Test
    void guardar_generaElNumeroDeOrdenCorrelativo() {
        when(compraRepository.count()).thenReturn(2L);
        Producto p = producto(5, 10);
        Compra r = service.guardar(compra(Compra.Estado.Pendiente, p, 1, "10.00"));
        assertEquals("OC-00003", r.getNumeroOrden());
    }

    // ───────────── CP-248: stock final = S + Q ─────────────
    @Test
    void guardar_compraRecibidaSumaLasUnidadesAlStock_integracion() {
        Producto p = producto(5, 10);
        service.guardar(compra(Compra.Estado.Recibida, p, 5, "20.00"));
        assertEquals(15, p.getStock());
        verify(productoRepository).save(p);
    }

    @Test
    void guardar_compraPendienteNoTocaElStock() {
        Producto p = producto(5, 10);
        service.guardar(compra(Compra.Estado.Pendiente, p, 5, "20.00"));
        assertEquals(10, p.getStock());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_pasarDePendienteARecibidaSumaElStockUnaSolaVez() {
        Producto p = producto(5, 10);
        Compra anterior = new Compra();
        anterior.setId(1);
        anterior.setEstado(Compra.Estado.Pendiente);
        when(compraRepository.findById(1)).thenReturn(Optional.of(anterior));

        Compra c = compra(Compra.Estado.Recibida, p, 5, "20.00");
        c.setId(1);
        service.guardar(c);

        assertEquals(15, p.getStock());
    }

    @Test
    void guardar_volverAGuardarUnaCompraYaRecibidaNoDuplicaElStock() {
        Producto p = producto(5, 10);
        Compra anterior = new Compra();
        anterior.setId(1);
        anterior.setEstado(Compra.Estado.Recibida);
        when(compraRepository.findById(1)).thenReturn(Optional.of(anterior));

        Compra c = compra(Compra.Estado.Recibida, p, 5, "20.00");
        c.setId(1);
        service.guardar(c);

        assertEquals(10, p.getStock());
        verify(productoRepository, never()).save(any());
    }

    // ───────────── CP-095 / H0059: movimiento de stock (se espera ROJO: CompraService no lo registra) ─────────────
    @Test
    void guardar_compraRecibidaRegistraUnMovimientoDeEntrada() {
        Producto p = producto(5, 10);
        service.guardar(compra(Compra.Estado.Recibida, p, 5, "20.00"));

        org.mockito.ArgumentCaptor<Inventario> cap = org.mockito.ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(cap.capture());
        assertEquals(Inventario.TipoMovimiento.Entrada, cap.getValue().getTipo());
        assertEquals(5, cap.getValue().getCantidad());
        assertEquals(10, cap.getValue().getStockAntes());
        assertEquals(15, cap.getValue().getStockDespues());
    }

    // ───────────── CP-249 a CP-251: validaciones (se espera ROJO: no existen en el servicio) ─────────────
    @Test
    void guardar_rechazaCantidadCero() {
        Producto p = producto(5, 10);
        assertThrows(ValidacionException.class,
                () -> service.guardar(compra(Compra.Estado.Recibida, p, 0, "20.00")));
    }

    @Test
    void guardar_rechazaCantidadNegativa_yNoRestaStock() {
        Producto p = producto(5, 10);
        assertThrows(ValidacionException.class,
                () -> service.guardar(compra(Compra.Estado.Recibida, p, -2, "20.00")));
        assertEquals(10, p.getStock());
    }

    @Test
    void guardar_rechazaPrecioDeCompraNegativo() {
        Producto p = producto(5, 10);
        assertThrows(ValidacionException.class,
                () -> service.guardar(compra(Compra.Estado.Pendiente, p, 1, "-5.00")));
    }

    @Test
    void guardar_rechazaUnaCompraSinProductos() {
        Compra c = new Compra();
        c.setProveedor(new Proveedor());
        assertThrows(ValidacionException.class, () -> service.guardar(c));
    }

    @Test
    void guardar_rechazaUnaCompraSinProveedor() {
        Producto p = producto(5, 10);
        Compra c = compra(Compra.Estado.Pendiente, p, 1, "10.00");
        c.setProveedor(null);
        assertThrows(ValidacionException.class, () -> service.guardar(c));
    }

    // ───────────── CP-253 a CP-256: listar y detalle ─────────────
    @Test
    void listarTodos_devuelveLasComprasDelRepositorio() {
        when(compraRepository.findAll()).thenReturn(List.of(new Compra(), new Compra()));
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void listarTodos_sinComprasDevuelveListaVaciaSinError() {
        when(compraRepository.findAll()).thenReturn(List.of());
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void buscarPorId_existenteDevuelveLaCompra() {
        Compra c = new Compra();
        c.setId(1);
        when(compraRepository.findById(1)).thenReturn(Optional.of(c));
        assertSame(c, service.buscarPorId(1));
    }

    @Test
    void buscarPorId_inexistenteDevuelveNull() {
        when(compraRepository.findById(404)).thenReturn(Optional.empty());
        assertNull(service.buscarPorId(404));
    }
}
