package com.ferreteria.v1.repositories;
 
import com.ferreteria.v1.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrue();
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);
    List<Producto> findByStockLessThanEqualAndActivoTrue(Integer stock);
    Optional<Producto> findByCodigo(String codigo);
}