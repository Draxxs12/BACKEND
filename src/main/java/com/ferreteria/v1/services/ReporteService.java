package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import com.ferreteria.v1.repositories.VentaRepository;

@Service
public class ReporteService {

    @Autowired private VentaRepository    ventaRepository;
    @Autowired private CompraRepository   compraRepository;
    @Autowired private ProductoRepository productoRepository;

    /** Libro Mayor Financiero: ingresos por ventas, egresos por compras, CxP pendientes. */
    public Map<String, Object> libroMayor(LocalDate desde, LocalDate hasta) {
        Map<String, Object> r = new LinkedHashMap<>();

        double ingresos = 0.0;
        List<Map<String, Object>> filasVentas = new ArrayList<>();
        for (Venta v : ventaRepository.findAll()) {
            if (v.getCreatedAt() == null) continue;
            LocalDate f = v.getCreatedAt().toLocalDate();
            if (enRango(f, desde, hasta) && v.getEstado() == Venta.EstadoVenta.Completada) {
                ingresos += v.getTotal() != null ? v.getTotal() : 0.0;
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fecha", f.toString());
                fila.put("comprobante", v.getNumeroComprobante());
                fila.put("cliente", v.getCliente() != null ? v.getCliente().getNombre() : "Cliente General");
                fila.put("total", v.getTotal());
                filasVentas.add(fila);
            }
        }

        double egresos = 0.0;
        double cuentasPorPagar = 0.0;
        List<Map<String, Object>> filasCompras = new ArrayList<>();
        for (Compra c : compraRepository.findAll()) {
            if (c.getCreatedAt() == null) continue;
            LocalDate f = c.getCreatedAt().toLocalDate();
            if (!enRango(f, desde, hasta)) continue;
            double total = c.getTotal() != null ? c.getTotal().doubleValue() : 0.0;
            if (c.getEstado() == Compra.Estado.Recibida) {
                egresos += total;
            } else if (c.getEstado() == Compra.Estado.Pendiente) {
                cuentasPorPagar += total;
            }
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", f.toString());
            fila.put("orden", c.getNumeroOrden());
            fila.put("proveedor", c.getProveedor() != null ? c.getProveedor().getEmpresa() : "—");
            fila.put("estado", c.getEstado().name());
            fila.put("total", total);
            filasCompras.add(fila);
        }

        r.put("desde", desde != null ? desde.toString() : null);
        r.put("hasta", hasta != null ? hasta.toString() : null);
        r.put("totalIngresos", redondear(ingresos));
        r.put("totalEgresos", redondear(egresos));
        r.put("cuentasPorPagar", redondear(cuentasPorPagar));
        r.put("utilidadBruta", redondear(ingresos - egresos));
        r.put("ventas", filasVentas);
        r.put("compras", filasCompras);
        return r;
    }

    /** Auditoría de Almacén: stock exacto del momento y capital almacenado. */
    public Map<String, Object> auditoriaStock() {
        Map<String, Object> r = new LinkedHashMap<>();
        List<Map<String, Object>> filas = new ArrayList<>();
        double capital = 0.0;
        for (Producto p : productoRepository.findByActivoTrue()) {
            double valor = (p.getStock() != null ? p.getStock() : 0)
                    * (p.getPrecioCompra() != null ? p.getPrecioCompra() : 0.0);
            capital += valor;
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", p.getCodigo());
            fila.put("nombre", p.getNombre());
            fila.put("categoria", p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría");
            fila.put("stock", p.getStock());
            fila.put("stockMinimo", p.getStockMinimo());
            fila.put("precioCompra", p.getPrecioCompra());
            fila.put("valorStock", redondear(valor));
            filas.add(fila);
        }
        r.put("capitalAlmacenado", redondear(capital));
        r.put("totalProductos", filas.size());
        r.put("productos", filas);
        return r;
    }

    private boolean enRango(LocalDate f, LocalDate desde, LocalDate hasta) {
        if (desde != null && f.isBefore(desde)) return false;
        if (hasta != null && f.isAfter(hasta))  return false;
        return true;
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
