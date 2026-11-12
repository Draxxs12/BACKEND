package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.projections.VentaReporteProjection;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByClienteId(Integer clienteId);
    List<Venta> findByUsuarioId(Integer usuarioId);
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    @Query("SELECT v.createdAt AS fecha, v.numeroComprobante AS comprobante, "
            + "COALESCE(c.nombre, 'Cliente General') AS cliente, v.total AS total "
            + "FROM Venta v LEFT JOIN v.cliente c "
            + "WHERE v.estado = com.ferreteria.v1.models.Venta.EstadoVenta.Completada "
            + "AND (:desde IS NULL OR v.createdAt >= :desde) "
            + "AND (:hastaExclusivo IS NULL OR v.createdAt < :hastaExclusivo) "
            + "ORDER BY v.createdAt DESC")
    List<VentaReporteProjection> buscarFilasParaLibroMayor(
            @Param("desde") LocalDateTime desde,
            @Param("hastaExclusivo") LocalDateTime hastaExclusivo);

    @Query("SELECT v.cliente.nombre, SUM(v.total) FROM Venta v "
           + "WHERE v.estado = 'Completada' AND v.cliente IS NOT NULL "
           + "GROUP BY v.cliente.nombre ORDER BY SUM(v.total) DESC")
    List<Object[]> topClientesPorVentas();
}
