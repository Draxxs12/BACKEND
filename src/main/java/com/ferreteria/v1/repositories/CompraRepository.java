package com.ferreteria.v1.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.repositories.projections.CompraReporteProjection;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :id")
    Optional<Compra> buscarPorIdParaActualizar(@Param("id") Integer id);

    @Query("SELECT c.createdAt AS fecha, c.numeroOrden AS orden, "
            + "p.empresa AS proveedor, c.estado AS estado, c.total AS total "
            + "FROM Compra c LEFT JOIN c.proveedor p "
            + "WHERE (:desde IS NULL OR c.createdAt >= :desde) "
            + "AND (:hastaExclusivo IS NULL OR c.createdAt < :hastaExclusivo) "
            + "ORDER BY c.createdAt DESC")
    List<CompraReporteProjection> buscarFilasParaLibroMayor(
            @Param("desde") LocalDateTime desde,
            @Param("hastaExclusivo") LocalDateTime hastaExclusivo);
}
