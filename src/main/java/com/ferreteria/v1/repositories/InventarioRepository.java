package com.ferreteria.v1.repositories;
 
import com.ferreteria.v1.models.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    List<Inventario> findByProductoId(Integer productoId);
    List<Inventario> findByTipo(Inventario.TipoMovimiento tipo);
}