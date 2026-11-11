package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.Venta;
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

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente "
            + "WHERE (:desde IS NULL OR v.createdAt >= :desde) "
            + "AND (:hastaExclusivo IS NULL OR v.createdAt < :hastaExclusivo) "
            + "ORDER BY v.createdAt DESC")
    List<Venta> buscarParaLibroMayor(
            @Param("desde") LocalDateTime desde,
            @Param("hastaExclusivo") LocalDateTime hastaExclusivo);

    @Query("SELECT v.cliente.nombre, SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'Completada' AND v.cliente IS NOT NULL " +
           "GROUP BY v.cliente.nombre " +
           "ORDER BY SUM(v.total) DESC")
    List<Object[]> topClientesPorVentas();
}