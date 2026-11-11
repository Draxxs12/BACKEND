package com.ferreteria.v1.repositories;
 
import com.ferreteria.v1.models.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrue();
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);
    List<Producto> findByStockLessThanEqualAndActivoTrue(Integer stock);
    Optional<Producto> findByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> buscarPorIdParaActualizar(@Param("id") Integer id);
}