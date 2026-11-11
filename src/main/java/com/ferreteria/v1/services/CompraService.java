package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.models.Compra.Estado;
import com.ferreteria.v1.models.DetalleCompra;
import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;

@Service
public class CompraService {

    @Autowired private CompraRepository        compraRepository;

    @Autowired private ProductoRepository      productoRepository;

    @Autowired private InventarioRepository    inventarioRepository;

    public List<Compra> listarTodos() {
        return compraRepository.findAll();
    }

    public Compra buscarPorId(Integer id) {
        return compraRepository.findById(id).orElse(null);
    }

    @Transactional
    public Compra guardar(Compra compra) {

        validarCompra(compra);

        boolean esNueva = (compra.getId() == null);

        Estado estadoAnterior = null;
        if (!esNueva) {
            Compra anterior = compraRepository.buscarPorIdParaActualizar(compra.getId()).orElse(null);
            if (anterior != null) estadoAnterior = anterior.getEstado();
        }

        if (esNueva && (compra.getNumeroOrden() == null || compra.getNumeroOrden().isEmpty())) {
            long count = compraRepository.count();
            String numeroOrden = String.format("OC-%05d", count + 1);
            compra.setNumeroOrden(numeroOrden);
        }

        List<DetalleCompra> detalles = compra.getDetalles();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleCompra d : detalles) {
            BigDecimal linea = d.getPrecioUnitario()
                                .multiply(BigDecimal.valueOf(d.getCantidad()));
            d.setSubtotal(linea);
            d.setCompra(compra);
            subtotal = subtotal.add(linea);
        }

        BigDecimal igv   = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(igv);

        compra.setSubtotal(subtotal);
        compra.setIgv(igv);
        compra.setTotal(total);

        Compra guardada = compraRepository.save(compra);

        boolean debeAumentarStock =
            guardada.getEstado() == Estado.Recibida &&
            (esNueva || estadoAnterior != Estado.Recibida);

        if (debeAumentarStock) {
            for (DetalleCompra d : guardada.getDetalles()) {
                Producto p = productoRepository
                                .buscarPorIdParaActualizar(d.getProducto().getId())
                                .orElse(null);
                if (p != null) {
                    int stockAntes = p.getStock() == null ? 0 : p.getStock();
                    int stockDespues = stockAntes + d.getCantidad();
                    p.setStock(stockDespues);
                    productoRepository.save(p);

                    Inventario movimiento = new Inventario();
                    movimiento.setProducto(p);
                    movimiento.setUsuario(guardada.getUsuario());
                    movimiento.setTipo(Inventario.TipoMovimiento.Entrada);
                    movimiento.setCantidad(d.getCantidad());
                    movimiento.setStockAntes(stockAntes);
                    movimiento.setStockDespues(stockDespues);
                    movimiento.setMotivo("Compra recibida " + guardada.getNumeroOrden());
                    inventarioRepository.save(movimiento);
                }
            }
        }

        return guardada;
    }

    private void validarCompra(Compra compra) {
        if (compra == null) {
            throw new ValidacionException("Debes proporcionar los datos de la compra.");
        }

        if (compra.getProveedor() == null) {
            throw new ValidacionException("Debes seleccionar un proveedor para la compra.");
        }

        List<DetalleCompra> detalles = compra.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            throw new ValidacionException("La compra debe contener al menos un producto.");
        }

        for (DetalleCompra detalle : detalles) {
            if (detalle == null || detalle.getProducto() == null
                    || detalle.getProducto().getId() == null) {
                throw new ValidacionException("Debes seleccionar un producto válido para cada detalle.");
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new ValidacionException("La cantidad de cada producto debe ser mayor que cero.");
            }

            if (detalle.getPrecioUnitario() == null
                    || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidacionException("El precio de compra no puede ser nulo ni negativo.");
            }
        }
    }

    @Transactional
    public void eliminar(Integer id) {
        compraRepository.deleteById(id);
    }
}
