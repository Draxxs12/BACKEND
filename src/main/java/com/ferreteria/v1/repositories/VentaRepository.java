package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findByClienteId(Integer clienteId);
    List<Venta> findByUsuarioId(Integer usuarioId);
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    @Query("SELECT v.cliente.nombre, SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.estado = 'Completada' AND v.cliente IS NOT NULL " +
           "GROUP BY v.cliente.nombre " +
           "ORDER BY SUM(v.total) DESC")
    List<Object[]> topClientesPorVentas();
}