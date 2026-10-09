package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.exceptions.ValidacionException;
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

@Service
public class DevolucionService {

    @Autowired private DevolucionRepository devolucionRepository;
    @Autowired private VentaRepository      ventaRepository;
    @Autowired private ProductoRepository   productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;

    public List<Devolucion> listarTodas() {
        return devolucionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Devolucion buscarPorId(Integer id) {
        return devolucionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Devolucion registrar(Integer ventaId, String motivo,
                                Devolucion.TipoReembolso tipoReembolso,
                                List<DetalleDevolucion> detalles, Usuario usuario) {

        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada id=" + ventaId));

        validarDetalles(ventaId, detalles);

        Devolucion dev = new Devolucion();
        dev.setVenta(venta);
        dev.setUsuario(usuario);
        dev.setMotivo(motivo);
        dev.setTipoReembolso(tipoReembolso != null ? tipoReembolso : Devolucion.TipoReembolso.Efectivo);

        long count = devolucionRepository.count();
        dev.setNumeroNota(String.format("NC-%05d", count + 1));

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleDevolucion d : detalles) {
            Producto producto = productoRepository.findById(d.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado id=" + d.getProducto().getId()));

            BigDecimal linea = d.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(d.getCantidad()));
            d.setSubtotal(linea);
            d.setProducto(producto);
            d.setDevolucion(dev);
            total = total.add(linea);

            // Devolver al inventario (Entrada)
            int stockAntes   = producto.getStock();
            int stockDespues = stockAntes + d.getCantidad();
            producto.setStock(stockDespues);
            productoRepository.save(producto);

            Inventario mov = new Inventario();
            mov.setProducto(producto);
            mov.setUsuario(usuario);
            mov.setTipo(Inventario.TipoMovimiento.Entrada);
            mov.setCantidad(d.getCantidad());
            mov.setStockAntes(stockAntes);
            mov.setStockDespues(stockDespues);
            mov.setMotivo("Devolución " + dev.getNumeroNota());
            inventarioRepository.save(mov);
        }

        dev.setMontoReembolso(total);
        dev.getDetalles().addAll(detalles);
        return devolucionRepository.save(dev);
    }

    /**
     * Valida TODAS las líneas antes de modificar stock o guardar algo:
     * cantidad mayor que 0 y que no supere lo vendido en esa venta.
     */
    private void validarDetalles(Integer ventaId, List<DetalleDevolucion> detalles) {
        Map<Integer, Integer> vendido = new HashMap<>();
        for (DetalleVenta dv : detalleVentaRepository.findByVentaId(ventaId)) {
            vendido.merge(dv.getProducto().getId(), dv.getCantidad(), Integer::sum);
        }

        Map<Integer, Integer> aDevolver = new HashMap<>();
        for (DetalleDevolucion d : detalles) {
            if (d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new ValidacionException("La cantidad a devolver debe ser mayor que 0.");
            }
            aDevolver.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> e : aDevolver.entrySet()) {
            int max = vendido.getOrDefault(e.getKey(), 0);
            if (e.getValue() > max) {
                throw new ValidacionException("No se puede devolver " + e.getValue()
                        + " unidad(es) del producto id=" + e.getKey()
                        + ": en la venta solo se vendieron " + max + ".");
            }
        }
    }
}
