package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;
import com.ferreteria.v1.repositories.projections.CompraReporteProjection;
import com.ferreteria.v1.repositories.projections.StockReporteProjection;
import com.ferreteria.v1.repositories.projections.VentaReporteProjection;

@Service
public class ReporteService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private ProductoRepository productoRepository;

    /** Libro Mayor Financiero: proyecta solo las columnas utilizadas por el reporte. */
    public Map<String, Object> libroMayor(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        Map<String, Object> resultado = new LinkedHashMap<>();

        double ingresos = 0.0;
        List<Map<String, Object>> filasVentas = new ArrayList<>();
        for (VentaReporteProjection venta : ventaRepository.buscarFilasParaLibroMayor(inicio, finExclusivo)) {
            if (venta.getFecha() == null) continue;
            double total = venta.getTotal() != null ? venta.getTotal() : 0.0;
            ingresos += total;
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", venta.getFecha().toLocalDate().toString());
            fila.put("comprobante", venta.getComprobante());
            fila.put("cliente", venta.getCliente() != null ? venta.getCliente() : "Cliente General");
            fila.put("total", venta.getTotal());
            filasVentas.add(fila);
        }

        double egresos = 0.0;
        double cuentasPorPagar = 0.0;
        List<Map<String, Object>> filasCompras = new ArrayList<>();
        for (CompraReporteProjection compra : compraRepository.buscarFilasParaLibroMayor(inicio, finExclusivo)) {
            if (compra.getFecha() == null) continue;
            BigDecimal importe = compra.getTotal() != null ? compra.getTotal() : BigDecimal.ZERO;
            double total = importe.doubleValue();
            if (compra.getEstado() == Compra.Estado.Recibida) {
                egresos += total;
            } else if (compra.getEstado() == Compra.Estado.Pendiente) {
                cuentasPorPagar += total;
            }
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", compra.getFecha().toLocalDate().toString());
            fila.put("orden", compra.getOrden());
            fila.put("proveedor", compra.getProveedor() != null ? compra.getProveedor() : "—");
            fila.put("estado", compra.getEstado() != null ? compra.getEstado().name() : "");
            fila.put("total", total);
            filasCompras.add(fila);
        }

        resultado.put("desde", desde != null ? desde.toString() : null);
        resultado.put("hasta", hasta != null ? hasta.toString() : null);
        resultado.put("totalIngresos", redondear(ingresos));
        resultado.put("totalEgresos", redondear(egresos));
        resultado.put("cuentasPorPagar", redondear(cuentasPorPagar));
        resultado.put("utilidadBruta", redondear(ingresos - egresos));
        resultado.put("ventas", filasVentas);
        resultado.put("compras", filasCompras);
        return resultado;
    }

    /** Auditoría de almacén: consulta únicamente las columnas necesarias y la categoría. */
    public Map<String, Object> auditoriaStock() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        List<Map<String, Object>> filas = new ArrayList<>();
        double capital = 0.0;
        for (StockReporteProjection producto : productoRepository.buscarFilasParaAuditoriaStock()) {
            double stock = producto.getStock() != null ? producto.getStock() : 0;
            double precioCompra = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : 0.0;
            double valor = stock * precioCompra;
            capital += valor;

            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", producto.getCodigo());
            fila.put("nombre", producto.getNombre());
            fila.put("categoria", producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
            fila.put("stock", producto.getStock());
            fila.put("stockMinimo", producto.getStockMinimo());
            fila.put("precioCompra", producto.getPrecioCompra());
            fila.put("valorStock", redondear(valor));
            filas.add(fila);
        }
        resultado.put("capitalAlmacenado", redondear(capital));
        resultado.put("totalProductos", filas.size());
        resultado.put("productos", filas);
        return resultado;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
