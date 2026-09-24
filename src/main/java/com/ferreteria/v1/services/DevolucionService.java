package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.models.DetalleDevolucion;
import com.ferreteria.v1.models.Devolucion;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
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
}
