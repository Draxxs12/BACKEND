package com.ferreteria.v1.services;

import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Producto;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.InventarioRepository;
import com.ferreteria.v1.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventarioService {

    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ProductoRepository productoRepository;

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public List<Inventario> listarPorProducto(Integer productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    public Inventario guardar(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    /** Ajuste manual de stock: fija el nuevo stock y registra el movimiento. */
    @Transactional
    public Inventario ajustarStock(Integer productoId, Integer nuevoStock, String motivo, Usuario usuario) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + productoId));

        int stockAntes = producto.getStock();
        int diferencia = nuevoStock - stockAntes;
        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        Inventario mov = new Inventario();
        mov.setProducto(producto);
        mov.setUsuario(usuario);
        mov.setTipo(Inventario.TipoMovimiento.Ajuste);
        mov.setCantidad(Math.abs(diferencia));
        mov.setStockAntes(stockAntes);
        mov.setStockDespues(nuevoStock);
        mov.setMotivo(motivo != null ? motivo : "Ajuste manual de inventario");
        return inventarioRepository.save(mov);
    }
}
