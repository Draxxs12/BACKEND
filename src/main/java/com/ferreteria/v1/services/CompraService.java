package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.models.Compra.Estado;
import com.ferreteria.v1.models.DetalleCompra;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.repositories.CompraRepository;
import com.ferreteria.v1.repositories.ProductoRepository;

@Service
public class CompraService {

    @Autowired private CompraRepository        compraRepository;

    @Autowired private ProductoRepository      productoRepository;

    public List<Compra> listarTodos() {
        return compraRepository.findAll();
    }
    

    public Compra buscarPorId(Integer id) {
        return compraRepository.findById(id).orElse(null);
    }

    @Transactional
    public Compra guardar(Compra compra) {

        boolean esNueva = (compra.getId() == null);

        Estado estadoAnterior = null;
        if (!esNueva) {
            Compra anterior = compraRepository.findById(compra.getId()).orElse(null);
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
                                .findById(d.getProducto().getId())
                                .orElse(null);
                if (p != null) {
                    p.setStock(p.getStock() + d.getCantidad());
                    productoRepository.save(p);
                }
            }
        }

        return guardada;
    }
    
    @Transactional
    public void eliminar(Integer id) {
        compraRepository.deleteById(id);
    }
}