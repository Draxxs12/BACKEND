package com.ferreteria.v1.services;

import com.ferreteria.v1.exceptions.Validador;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioActualService usuarioActual;

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(nombre);
    }

    public Producto buscarPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto guardar(Producto producto) {
        Validador.noNegativo(producto.getPrecioVenta(),  "precio de venta");
        Validador.noNegativo(producto.getPrecioCompra(), "precio de compra");
        Validador.noNegativo(producto.getStock(),        "stock");
        Validador.noNegativo(producto.getStockMinimo(),  "stock mínimo");
        Validador.ventaMayorIgualCompra(producto.getPrecioVenta(), producto.getPrecioCompra());
        producto.setRegistradoPor(usuarioActual.nombreActual());
        return productoRepository.save(producto);
    }

    
    @Transactional
    public void eliminar(Integer id) {
        Producto p = productoRepository.findById(id).orElse(null);
        if (p != null) {
            p.setActivo(false);
            productoRepository.save(p);
            productoRepository.flush();
        }
    }

    public List<Producto> productosConStockBajo() {
        return productoRepository.findByStockLessThanEqualAndActivoTrue(5);
    }
}