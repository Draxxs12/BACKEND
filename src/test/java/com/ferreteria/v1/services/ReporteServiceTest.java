package com.ferreteria.v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;
import com.ferreteria.v1.repositories.projections.CompraReporteProjection;
import com.ferreteria.v1.repositories.projections.StockReporteProjection;
import com.ferreteria.v1.repositories.projections.VentaReporteProjection;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private ProductoRepository productoRepository;
    @InjectMocks private ReporteService reporteService;

    @Test
    void libroMayor_calculaIngresosEgresosYDeudaConProyecciones() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 10, 10, 30);
        VentaReporteProjection venta = mock(VentaReporteProjection.class);
        when(venta.getFecha()).thenReturn(fecha);
        when(venta.getComprobante()).thenReturn("B001-15");
        when(venta.getCliente()).thenReturn("Cliente prueba");
        when(venta.getTotal()).thenReturn(150.0);
        when(ventaRepository.buscarFilasParaLibroMayor(any(), any())).thenReturn(List.of(venta));

        CompraReporteProjection recibida = mock(CompraReporteProjection.class);
        when(recibida.getFecha()).thenReturn(fecha);
        when(recibida.getOrden()).thenReturn("OC-01");
        when(recibida.getProveedor()).thenReturn("Proveedor prueba");
        when(recibida.getEstado()).thenReturn(Compra.Estado.Recibida);
        when(recibida.getTotal()).thenReturn(new BigDecimal("40.00"));

        CompraReporteProjection pendiente = mock(CompraReporteProjection.class);
        when(pendiente.getFecha()).thenReturn(fecha);
        when(pendiente.getEstado()).thenReturn(Compra.Estado.Pendiente);
        when(pendiente.getTotal()).thenReturn(new BigDecimal("25.00"));
        when(compraRepository.buscarFilasParaLibroMayor(any(), any())).thenReturn(List.of(recibida, pendiente));

        Map<String, Object> resultado = reporteService.libroMayor(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

        assertEquals(150.0, resultado.get("totalIngresos"));
        assertEquals(40.0, resultado.get("totalEgresos"));
        assertEquals(25.0, resultado.get("cuentasPorPagar"));
        assertEquals(110.0, resultado.get("utilidadBruta"));
        assertEquals(1, ((List<?>) resultado.get("ventas")).size());
        assertEquals(2, ((List<?>) resultado.get("compras")).size());
    }

    @Test
    void auditoriaStock_calculaCapitalConSoloLasColumnasProyectadas() {
        StockReporteProjection producto = mock(StockReporteProjection.class);
        when(producto.getCodigo()).thenReturn("P-01");
        when(producto.getNombre()).thenReturn("Martillo");
        when(producto.getCategoria()).thenReturn("Herramientas");
        when(producto.getStock()).thenReturn(10);
        when(producto.getStockMinimo()).thenReturn(2);
        when(producto.getPrecioCompra()).thenReturn(7.5);
        when(productoRepository.buscarFilasParaAuditoriaStock()).thenReturn(List.of(producto));

        Map<String, Object> resultado = reporteService.auditoriaStock();

        assertEquals(75.0, resultado.get("capitalAlmacenado"));
        assertEquals(1, resultado.get("totalProductos"));
        assertEquals(1, ((List<?>) resultado.get("productos")).size());
    }

    @Test
    void libroMayor_rechazaRangoDeFechasInvertidoSinConsultarRepositorios() {
        assertThrows(IllegalArgumentException.class,
                () -> reporteService.libroMayor(LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1)));
        verifyNoInteractions(ventaRepository, compraRepository);
    }
}
